import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*

fun addButtons(buttonPanel: JPanel) {
    val nextGenButton = JButton("Next Gen")
    buttonPanel.add(nextGenButton)
    nextGenButton.addActionListener { nextGen() }

    val rulesButton = JButton("Change Rules")
    buttonPanel.add(rulesButton)
    rulesButton.addActionListener { openRules(game.rules) }

    val indicateMoveButton = JButton("indicate the number of moves")
    buttonPanel.add(indicateMoveButton)
    indicateMoveButton.addActionListener { openIndicateMoves() }

    val automaticMoveButton = JButton("Start automatic move")
    buttonPanel.add(automaticMoveButton)
    automaticMoveButton.addActionListener { StartMove = true }

    val stopAutomaticMoveButton = JButton("Stop automatic move")
    buttonPanel.add(stopAutomaticMoveButton)
    stopAutomaticMoveButton.addActionListener { StartMove = false }
}

fun openRules(rules: Rules) {
    val window = JFrame("Change Rules").apply {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        isVisible = true
        preferredSize = Dimension(350, 200)
        minimumSize = Dimension(350, 200)
    }
    val continuePanel = getPanel(rules.continue_to_live)
    val birthPanel = getPanel(rules.birth_of_live)
    window.add(continuePanel, BorderLayout.NORTH)
    window.add(birthPanel, BorderLayout.CENTER)
}

fun openIndicateMoves() {
    val window = JFrame("indicate the number of moves").apply {
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        isVisible = true
        preferredSize = Dimension(350, 150)
        minimumSize = Dimension(350, 150)
    }
    val text = JTextField()
    window.layout = (FlowLayout())
    window.add(text)
    text.isEditable = true
    text.columns = 20
    val button = JButton("OK")
    button.addActionListener {
        val n = text.text.toInt()
        repeat(n){
            nextGen()
        }
        window.defaultCloseOperation
    }
    window.add(button)
}

fun getPanel(array: Array<Boolean>): JPanel {
    val panel = JPanel()
    panel.isVisible = true
    panel.layout = GridLayout(1, array.size)
    val buttons: Array<JCheckBox> = Array(9) {
            i -> JCheckBox("$i").apply { isSelected = array[i] }
    }
    buttons.forEachIndexed{ i, checkBox ->
        checkBox.addActionListener { array[i] = !array[i] }
        panel.add(checkBox)
    }
    return panel
}
