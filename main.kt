import org.jetbrains.skia.*
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skiko.*
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import kotlin.math.max
import kotlin.random.Random


var Cell_size = 1
val Floor_size = 1024
var StartMove: Boolean = false
var Cells = List(Floor_size*Floor_size, ({ Cell()}))
val game: Game = Game()

class Game{
    var rules: Rules = Rules()
    var cameraPos = Pair(0, 0)
    fun drawCells(canvas: Canvas, color: Paint){
        for(i in 0 until Floor_size){
            for(j in 0 until Floor_size){
                val cell: Cell = Cells[i*Floor_size + j]
                if(cell.live == Condition.LIVE) {
                    val draw = cameraDraw(i, j)
                    canvas.drawRRect(draw, color)
                }
            }
        }
    }
    private fun cameraDraw(x: Int, y: Int): RRect{
        return RRect.Companion.makeLTRB(((x - cameraPos.first) * Cell_size).toFloat(), ((y - cameraPos.second) * Cell_size).toFloat(),
            ((x + 1 - cameraPos.first) * Cell_size).toFloat(), ((y + 1 - cameraPos.second) * Cell_size).toFloat(), 0F)
    }
}
class Rules{
    var continue_to_live: Array<Boolean> = Array(9, ({false}))
    var birth_of_live: Array<Boolean> = Array(9, ({false}))
    init{
        continue_to_live[2] = true
        continue_to_live[3] = true
        birth_of_live[3] = true
    }
}
fun addnearcells(){
    for(i in 0 until  Floor_size){
        for(j in 0 until Floor_size){
            val center = j * Floor_size + i
            Cells[center].near.add(Pair((j + 1)%Floor_size, i))
            Cells[center].near.add(Pair((Floor_size + j - 1)%Floor_size, i))
            Cells[center].near.add(Pair(j, (Floor_size + i - 1)%Floor_size))
            Cells[center].near.add(Pair(j, (i + 1)%Floor_size))
            Cells[center].near.add(Pair((Floor_size + j - 1)%Floor_size, (Floor_size + i - 1)%Floor_size))
            Cells[center].near.add(Pair((j + 1)%Floor_size, (Floor_size + i - 1)%Floor_size))
            Cells[center].near.add(Pair((Floor_size + j - 1)%Floor_size, (i + 1)%Floor_size))
            Cells[center].near.add(Pair((j + 1)%Floor_size, (i + 1)%Floor_size))
        }
    }
}
enum class Condition{
    NONE,
    LIVE,
    DEAD,
    BIRTH
}
class Cell{
    private var age: Int
    var live: Condition
    var near: MutableList<Pair<Int, Int>>
    init{
        age = 0
        live = Condition.NONE
        near = mutableListOf()
    }

    private fun nearlive(coordinates: Pair<Int, Int>): Boolean{
        return (Cells[coordinates.first*Floor_size + coordinates.second].live == Condition.LIVE ||
                Cells[coordinates.first*Floor_size + coordinates.second].live == Condition.DEAD)
    }

    fun step1(){
        var count = 0
        for(i in this.near){
            if(nearlive(i))count++
        }

        if(this.live == Condition.LIVE && game.rules.continue_to_live[count]){
            age++
        }

        else if(this.live == Condition.NONE && game.rules.birth_of_live[count]) {
            this.live = Condition.BIRTH
            this.age++
        }

        else if(this.live != Condition.NONE){
            this.live = Condition.DEAD
            this.age = 0
        }
    }
    fun step2(){
        if(this.live == Condition.DEAD)
            this.live = Condition.NONE

        if(this.live == Condition.BIRTH)
            this.live = Condition.LIVE
    }

    fun isBirth(){
        if(this.live != Condition.LIVE){
            this.live = Condition.LIVE
            this.age = 1
        }
    }
}

fun nextGen(){
    for(i in Cells){
        i.step1()
    }
    for(i in Cells){
        i.step2()
    }
}

fun click(x: Int, y: Int){
    val cordx = x/Cell_size
    val cordy = y/Cell_size
    if(cordx < Floor_size && cordy < Floor_size){
        Cells[cordx * Floor_size + cordy].isBirth()
    }
    else{
        nextGen()
    }
}

fun main() {
    addnearcells()
    val skiaLayer = SkiaLayer()
    skiaLayer.skikoView = GenericSkikoView(skiaLayer, object: SkikoView {
        val black = Paint().apply {
            color = Color.makeRGB(0,0 , 0)
        }
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.clear(Color.makeRGB(255,255, 255))
            game.drawCells(canvas, black)
            if(StartMove)nextGen()
        }
    })
    SwingUtilities.invokeLater {
        val window = JFrame("life").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isVisible = true
            preferredSize = Dimension(1200, 1200)
            minimumSize = Dimension(1200,1200)
        }
        skiaLayer.addMouseListener(object: MouseListener {
            override fun mouseClicked(e: MouseEvent) { click(e.x + game.cameraPos.first * Cell_size, e.y + game.cameraPos.second * Cell_size) }

            override fun mouseExited(e: MouseEvent) {}
            override fun mousePressed(e: MouseEvent) {}
            override fun mouseEntered(e: MouseEvent) {}
            override fun mouseReleased(e: MouseEvent) {}
        })

        skiaLayer.addMouseWheelListener { e ->
            run {
                Cell_size = max(1, Cell_size - e.wheelRotation)
            }
        }

        skiaLayer.addKeyListener(object: KeyListener {
            override fun keyPressed(e: KeyEvent?) {}
            override fun keyReleased(e: KeyEvent?) {}
            override fun keyTyped(e: KeyEvent?) {
                val code: Char = e?.keyChar ?: '0'
                game.cameraPos = when (code) {
                    'w' -> Pair(game.cameraPos.first, game.cameraPos.second - 1)
                    's' -> Pair(game.cameraPos.first, game.cameraPos.second + 1)
                    'a' -> Pair(game.cameraPos.first - 1, game.cameraPos.second)
                    'd' -> Pair(game.cameraPos.first + 1, game.cameraPos.second)
                    else -> game.cameraPos
                }
            }
        })

        val buttonPanel = JPanel()
        val layout = GridLayout(5 , 1)
        layout.columns = 1
        buttonPanel.layout = layout
        addButtons(buttonPanel)

        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true

        window.add(buttonPanel, BorderLayout.EAST)
    }
}
