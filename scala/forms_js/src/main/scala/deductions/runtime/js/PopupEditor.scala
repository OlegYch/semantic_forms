package deductions.runtime.js

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js.JSApp
import scala.scalajs.js
import js.Dynamic.literal
import org.scalajs.dom.raw.BeforeUnloadEvent

// @JSExport
/** outdated, currently we use the JavaScript in forms_play/public/ */
object PopupEditor extends JSApp {

  @JSExport
  def main() = ()

  @JSExport
  def launchEditorWindow(input: html.Input): Unit = {

    // setup new window
    val popupWindow = dom.window.open("",
      "Edit_Markdown_text_for_semantic_forms",
      "height=500,width=500,resizable=yes,modal=yes")
    val closeButton = makeButton(popupWindow, "SAVE")
    val exitButton = makeButton(popupWindow, "DISMISS")

    val editorDiv = popupWindow.document.createElement("div")
    val body = popupWindow.document.body
    body.appendChild(closeButton)
    body.appendChild(exitButton)
    body.appendChild(editorDiv)

    // launch Pen editor
    val child = popupWindow.document.createElement("script")
    val typ = popupWindow.document.createAttribute("type")
      typ.value = "text/javascript"
      child.attributes.setNamedItem(typ)
    val src = popupWindow.document.createAttribute("src")
      src.value = "https://rawgit.com/sofish/pen/master/src/markdown.js"
      child.attributes.setNamedItem(src)
    popupWindow.document.head.appendChild( child )

    /* <script type="text/javascript" async="true"
     * src="https://rawgit.com/sofish/pen/master/src/markdown.js"></script> */

    val options = literal(
      "editor" -> editorDiv,
      "class" -> "pen",
      "debug" -> true,
      "list" -> // editor menu list
        js.Array("insertimage", "blockquote", "h2", "h3", "p", "code",
          "insertorderedlist", "insertunorderedlist", "inserthorizontalrule",
          "indent", "outdent", "bold", "italic", "underline", "createlink")
    )
    val pen = js.Dynamic.global.Pen
    val editor = js.Dynamic.newInstance(pen)(options)

    editorDiv.innerHTML = input.value
    if (input.value == "")
      editorDiv.innerHTML = "?"

    // to avoid message "data you have entered may not be saved."
    dom.window.onbeforeunload = (_: BeforeUnloadEvent) => {
      log("popupWindow.onbeforeunload")
    }

    def onClose() = {
      log("popupWindow.onunload")
      val stringToSave = editor.toMd().toString() // return a markdown string
      log("popupWindow.onunload stringToSave " + stringToSave )
      if (stringToSave != input.value) {
          log("popupWindow.onunload: saving because " + stringToSave +
            " != " + input.value)
          input.value = stringToSave
      } else {
        log("popupWindow.onunload: nothing to save")
      }
      body.innerHTML = ""
    }

    closeButton.onclick = (_: dom.MouseEvent) => {
      onClose();
      popupWindow.close()
    }
    exitButton.onclick = (_: dom.MouseEvent) => popupWindow.close()
  }

  def makeButton(window: org.scalajs.dom.raw.Window, label: String): html.Button = {
    val button = window.document.createElement("button").
      asInstanceOf[html.Button]
    button.innerHTML = label
    button.textContent = label
    button
  }

  def log(m: String) = dom.console.log(m)
}
