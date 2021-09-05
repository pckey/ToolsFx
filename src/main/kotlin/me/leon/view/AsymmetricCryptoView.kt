package me.leon.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import me.leon.base.base64
import me.leon.controller.AsymmetricCryptoController
import me.leon.ext.*
import tornadofx.*

class AsymmetricCryptoView : View(FX.messages["asymmetric"]) {
    private val controller: AsymmetricCryptoController by inject()
    override val closeable = SimpleBooleanProperty(false)
    private val privateKeyEncrypt = SimpleBooleanProperty(false)
    lateinit var input: TextArea
    lateinit var key: TextArea
    lateinit var output: TextArea
    private val inputText: String
        get() = input.text
    private val outputText: String
        get() = output.text
    private val info
        get() =
            "RSA  bits: ${selectedBits.get()}  mode: ${if (privateKeyEncrypt.get()) "private key encrypt" 
        else "public key encrypt"} "
    private lateinit var infoLabel: Label
    private val keyText: String
        get() =
            key.text.takeIf { it.contains("-----BEGIN CERTIFICATE") }
                ?: key.text.replace(
                    "-----(?:END|BEGIN) (?:RSA )?\\w+ KEY-----|\n|\r|\r\n".toRegex(),
                    ""
                )

    private var alg = "RSA"
    private var isEncrypt = true
    private val bitsLists = mutableListOf("512", "1024", "2048", "3072", "4096")
    private val selectedBits = SimpleStringProperty("1024")
    private val isPriEncryptOrPubDecrypt
        get() = privateKeyEncrypt.get() && isEncrypt || !privateKeyEncrypt.get() && !isEncrypt

    private val eventHandler = fileDraggedHandler {
        val firstFile = it.first()
        key.text =
            if (firstFile.name.endsWith("pk8")) firstFile.readBytes().base64()
            else firstFile.readText()

        with(keyText) {
            val probablyKeySize =
                if (isPriEncryptOrPubDecrypt) this.length * 1.25f else this.length * 5
            println("__ $probablyKeySize")
            val keySize =
                when (probablyKeySize.toInt()) {
                    in 3300..4500 -> 4096
                    in 2600..3300 -> 3072
                    in 1600..2200 -> 2048
                    in 800..1200 -> 1024
                    else -> 512
                }
            selectedBits.set(keySize.toString())
        }
    }
    private val centerNode = vbox {
        paddingAll = DEFAULT_SPACING
        spacing = DEFAULT_SPACING
        hbox {
            label("密钥:")
            button(graphic = imageview(Image("/import.png"))) {
                action { input.text = clipboardText() }
            }
        }
        key =
            textarea {
                promptText = "请输入密钥或者拖动文件到此区域"
                isWrapText = true
                onDragEntered = eventHandler
            }

        hbox {
            label("待处理 (明文/base64密文):") { tooltip("加密时为明文,解密时为base64编码的密文") }
            button(graphic = imageview(Image("/import.png"))) {
                action { input.text = clipboardText() }
            }
        }
        input =
            textarea {
                promptText = "请输入或者拖动文件到此区域"
                isWrapText = true
                prefHeight = DEFAULT_SPACING_10X
            }

        hbox {
            alignment = Pos.CENTER_LEFT
            label("位数：")
            combobox(selectedBits, bitsLists) { cellFormat { text = it } }
            togglegroup {
                spacing = DEFAULT_SPACING
                radiobutton("加密") { isSelected = true }
                radiobutton("解密")
                selectedToggleProperty().addListener { _, _, new ->
                    isEncrypt = (new as RadioButton).text == "加密"
                }
            }

            checkbox("私钥加密", privateKeyEncrypt) { tooltip("默认公钥加密，私钥解密。开启后私钥加密，公钥解密") }

            button(messages["run"], imageview(Image("/run.png"))) { action { doCrypto() } }
            button("生成公私钥") { action { "https://miniu.alipay.com/keytool/create".openInBrowser() } }
        }
        hbox {
            spacing = DEFAULT_SPACING
            label("输出内容:")
            button(graphic = imageview(Image("/copy.png"))) { action { outputText.copy() } }
            button(graphic = imageview(Image("/up.png"))) {
                action {
                    input.text = outputText
                    output.text = ""
                }
            }
        }
        output =
            textarea {
                promptText = "结果"
                isWrapText = true
            }
    }
    override val root = borderpane {
        center = centerNode
        bottom = hbox { infoLabel = label(info) }
    }

    private fun doCrypto() {
        if (keyText.isEmpty() || inputText.isEmpty()) {
            output.text = "请输入key 或者 待处理内容"
            return
        }

        runAsync {
            if (isEncrypt)
                if (privateKeyEncrypt.get())
                    controller.priEncrypt(keyText, alg, inputText, selectedBits.get().toInt())
                else controller.pubEncrypt(keyText, alg, inputText, selectedBits.get().toInt())
            else if (privateKeyEncrypt.get())
                controller.pubDecrypt(keyText, alg, inputText, selectedBits.get().toInt())
            else controller.priDecrypt(keyText, alg, inputText, selectedBits.get().toInt())
        } ui
            {
                output.text = it
                infoLabel.text = info
            }
    }
}
