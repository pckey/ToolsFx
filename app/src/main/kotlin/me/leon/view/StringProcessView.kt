package me.leon.view

import java.io.File
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.*
import kotlin.system.measureTimeMillis
import me.leon.*
import me.leon.ext.*
import me.leon.ext.fx.*
import tornadofx.*
import tornadofx.FX.Companion.messages

class StringProcessView : Fragment(messages["stringProcess"]) {

    private var timeConsumption = 0L
    override val closeable = SimpleBooleanProperty(false)
    private val regexp = SimpleBooleanProperty(false)
    private val splitRegexp = SimpleBooleanProperty(false)
    private val fileMode = SimpleBooleanProperty(false)
    private val showAddition = SimpleBooleanProperty(false)
    private val showAdditionCheck = SimpleBooleanProperty(false)

    private var taInput: TextArea by singleAssign()
    private var taInput2: TextArea by singleAssign()
    private var taOutput: TextArea by singleAssign()
    private var tfReplaceFrom: TextField by singleAssign()
    private var tfReplaceTo: TextField by singleAssign()
    private var tfSplitLength: TextField by singleAssign()
    private var tfSeparator: TextField by singleAssign()
    private var labelInfo: Label by singleAssign()
    private var tfExtract: TextField by singleAssign()

    private val replaceFromText
        get() = tfReplaceFrom.text.unescape()

    private val replaceToText
        get() = tfReplaceTo.text.unescape()

    private val splitLengthText
        get() =
            runCatching { tfSplitLength.text.toInt() }
                .getOrElse {
                    tfSplitLength.text = "8"
                    8
                }

    private val separatorText
        get() = tfSeparator.text.unescape()

    private val info: String
        get() =
            " ${messages["inputLength"]}: " +
                "${inputText.length}  ${messages["outputLength"]}: ${outputText.length} " +
                "lines(in/out): ${inputText.lineCount()} / ${outputText.lineCount()} " +
                "cost: $timeConsumption ms"

    private var inputText: String
        get() = taInput.text
        set(value) {
            taInput.text = value
        }

    private var inputText2: String
        get() = taInput2.text
        set(value) {
            taInput2.text = value
        }

    private var outputText: String
        get() = taOutput.text
        set(value) {
            taOutput.text = value
        }

    private val extractReg
        get() = tfExtract.text.unescape()

    private val eventHandler = fileDraggedHandler {
        taInput.text =
            with(it.first()) {
                if (fileMode.get()) {
                    absolutePath
                } else {
                    it.first().properText()
                }
            }
    }
    private val centerNode = vbox {
        addClass(Styles.group)
        hbox {
            label(messages["input"])
            spacing = DEFAULT_SPACING
            button(graphic = imageview(IMG_IMPORT)) {
                tooltip(messages["pasteFromClipboard"])
                action {
                    inputText = clipboardText()
                    timeConsumption = 0
                    labelInfo.text = info
                }
            }
            button(graphic = imageview("/img/uppercase.png")) {
                tooltip(messages["uppercase"])
                action { outputText = inputText.uppercase() }
            }

            button(graphic = imageview("/img/lowercase.png")) {
                tooltip(messages["lowercase"])
                action { outputText = inputText.lowercase() }
            }
            button(graphic = imageview("/img/trimIndent.png")) {
                tooltip(messages["trimIndent"])
                action { outputText = inputText.trimIndent() }
            }
            button(graphic = imageview("/img/ascend.png")) {
                tooltip(messages["orderByStringASC"])
                action {
                    measureTimeMillis {
                            outputText =
                                inputText
                                    .split("\n|\r\n".toRegex())
                                    .sorted()
                                    .joinToString(System.lineSeparator())
                        }
                        .also {
                            timeConsumption = it
                            labelInfo.text = info
                        }
                }
            }
            button(graphic = imageview("/img/descend.png")) {
                tooltip(messages["orderByStringDESC"])
                action {
                    measureTimeMillis {
                            outputText =
                                inputText
                                    .split("\n|\r\n".toRegex())
                                    .sortedDescending()
                                    .joinToString(System.lineSeparator())
                        }
                        .also {
                            timeConsumption = it
                            labelInfo.text = info
                        }
                }
            }

            button(graphic = imageview("/img/deduplicate.png")) {
                tooltip(messages["deduplicateLine"])
                action {
                    measureTimeMillis {
                            outputText =
                                inputText.lines().distinct().joinToString(System.lineSeparator())
                        }
                        .also {
                            timeConsumption = it
                            labelInfo.text = info
                        }
                }
            }
            button(graphic = imageview("/img/statistic.png")) {
                tooltip(messages["letterStatistics"])
                action {
                    measureTimeMillis {
                            outputText =
                                inputText
                                    .groupingBy { it }
                                    .eachCount()
                                    .toList()
                                    .filter { it.first.code > 32 }
                                    .sortedByDescending { it.second }
                                    .joinToString(System.lineSeparator()) {
                                        "${it.first}: ${it.second}"
                                    }
                        }
                        .also {
                            timeConsumption = it
                            labelInfo.text = info
                        }
                }
            }
        }

        taInput = textarea {
            promptText = messages["inputHint"]
            isWrapText = true
            onDragEntered = eventHandler
            contextmenu {
                item(messages["loadFromNet"]) {
                    action { runAsync { inputText.readFromNet() } ui { taInput.text = it } }
                }
                item(messages["recoverEncoding"]) {
                    action { runAsync { inputText.recoverEncoding() } ui { taInput.text = it } }
                }
                item(messages["reverse"]) { action { taInput.text = inputText.reversed() } }
                item(messages["removeAllSpaceByLine"]) {
                    action {
                        taInput.text =
                            inputText
                                .lines()
                                .map { it.stripAllSpace() }
                                .filterNot { it.isEmpty() }
                                .joinToString(System.lineSeparator())
                    }
                }
                item(messages["removeAllSpace"]) {
                    action { taInput.text = inputText.stripAllSpace() }
                }
                item("input - addition") {
                    action {
                        val (inputs, inputs2) = inputsList()
                        taOutput.text =
                            inputs
                                .filterNot { inputs2.contains(it) }
                                .joinToString(System.lineSeparator())
                        showAddition.value = false
                        labelInfo.text = info
                    }
                }
                item("input ∪ addition") {
                    action {
                        val (inputs, inputs2) = inputsList()
                        taOutput.text =
                            (inputs + inputs2).distinct().joinToString(System.lineSeparator())
                        showAddition.value = false
                        labelInfo.text = info
                    }
                }
                item("input ∩ addition") {
                    action {
                        val (inputs, inputs2) = inputsList()
                        taOutput.text =
                            inputs
                                .filter { inputs2.contains(it) }
                                .joinToString(System.lineSeparator())
                        showAddition.value = false
                        labelInfo.text = info
                    }
                }
            }
            textProperty().addListener { _, _, _ ->
                timeConsumption = 0
                labelInfo.text = info
            }
        }
        hbox {
            addClass(Styles.left)
            paddingTop = DEFAULT_SPACING
            paddingBottom = DEFAULT_SPACING
            label(messages["replace"])
            tfReplaceFrom = textfield { promptText = messages["text2Replaced"] }
            tfReplaceTo = textfield { promptText = messages["replaced"] }
            checkbox(messages["regexp"], regexp)
            button(messages["run"], imageview(IMG_RUN)) { action { doReplace() } }
            checkbox(messages["fileMode"], fileMode)
        }
        hbox {
            addClass(Styles.left)
            paddingTop = DEFAULT_SPACING
            paddingBottom = DEFAULT_SPACING
            label(messages["split"])
            tfSplitLength = textfield { promptText = messages["splitLength"] }
            tfSeparator = textfield { promptText = messages["delimiter"] }
            checkbox(messages["regexp"], splitRegexp)
            button(messages["run"], imageview(IMG_RUN)) { action { doSplit() } }
        }

        hbox {
            addClass(Styles.left)
            paddingTop = DEFAULT_SPACING
            paddingBottom = DEFAULT_SPACING
            label(messages["extract"])
            tfExtract = textfield {
                promptText = messages["extractHint"]
                prefWidth = 330.0
            }
            checkbox(messages["regexp"]) { isVisible = false }
            button(messages["run"], imageview(IMG_RUN)) { action { doExtract() } }
            checkbox(messages["fileMode"], fileMode)
        }

        hbox {
            spacing = DEFAULT_SPACING
            label(messages["output"])
            button(graphic = imageview(IMG_COPY)) {
                tooltip(messages["copy"])
                action { outputText.copy() }
            }
            button(graphic = imageview(IMG_UP)) {
                tooltip(messages["up"])
                action {
                    taInput.text = outputText
                    taOutput.text = ""
                }
            }

            hbox {
                addClass(Styles.center)
                checkbox("show addition", showAddition) { visibleWhen(showAdditionCheck) }
            }
        }
        stackpane {
            prefHeight = 300.0
            spacing = 8.0
            taInput2 = textarea {
                promptText = "additional input or output"
                isWrapText = true
                visibleWhen(showAddition)
            }
            taOutput = textarea {
                promptText = messages["outputHint"]
                visibleWhen(!showAddition)
                isWrapText = true
            }
        }
        subscribe<SimpleMsgEvent> { inputText = it.msg }
    }

    override val root = borderpane {
        center = centerNode
        bottom = hbox { labelInfo = label(info) }
    }

    private fun inputsList(): Pair<List<String>, List<String>> {
        showAdditionCheck.value = true
        val inputs = inputText.lines().map { it.stripAllSpace() }.filterNot { it.isEmpty() }
        val inputs2 = inputText2.lines().map { it.stripAllSpace() }.filterNot { it.isEmpty() }
        return Pair(inputs, inputs2)
    }

    private fun doExtract() {
        measureTimeMillis {
                outputText =
                    extractReg
                        .toRegex()
                        .findAll(if (fileMode.get()) inputText.toFile().readText() else inputText)
                        .map { it.value }
                        .joinToString("\n")
            }
            .also {
                timeConsumption = it
                labelInfo.text = info
            }
    }

    private fun doSplit() {

        measureTimeMillis {
                outputText =
                    if (splitRegexp.get()) {
                        inputText
                            .split(tfSplitLength.text.unescape().toRegex())
                            .joinToString(separatorText)
                    } else {
                        inputText.asIterable().chunked(splitLengthText).joinToString(
                            separatorText
                        ) {
                            it.joinToString("")
                        }
                    }
            }
            .also {
                timeConsumption = it
                labelInfo.text = info
            }
    }

    private fun doReplace() {
        measureTimeMillis {
                if (replaceFromText.isNotEmpty()) {
                    println(replaceToText)
                    outputText = if (fileMode.get()) renameFiles() else replaceStr(inputText)
                }
            }
            .also {
                timeConsumption = it
                labelInfo.text = info
            }
    }

    private fun replaceStr(name: String) =
        if (regexp.get()) {
            name.replace(replaceFromText.toRegex(), replaceToText)
        } else {
            name.replace(replaceFromText, replaceToText)
        }

    private fun renameFiles(): String {
        return inputText
            .lineAction {
                val file = it.toFile()
                if (file.exists().not()) {
                    return "$it file not exists!"
                }
                if (file.isDirectory) {
                    file
                        .walk()
                        .filter(File::isFile)
                        .filter { it.name != replaceStr(it.name) }
                        .map { f ->
                            File(f.parent, replaceStr(f.name)).also { f.renameTo(it) }.absolutePath
                        }
                        .joinToString(System.lineSeparator())
                } else {
                    File(file.parent, replaceStr(file.name)).also { file.renameTo(it) }.absolutePath
                }
            }
            .joinToString(System.lineSeparator())
    }
}
