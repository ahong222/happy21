package xin.kotlin.happy21.game

import java.util.*

/**
 * Created by shen on 17/5/29.
 */
class GameUtils {
    var cardCount = 4
    var includeKing = false
    /**
     * color值 4=黑桃,3=红桃,2=梅花,1=方片
     */
    fun getWashedCards(): ArrayList<Int> {
        var size = if(includeKing)54 else 52
        val tempCard: Array<Int> = Array(size * cardCount, { i -> (i%size)+1 })

        val length = tempCard.size
        val random = Random()
        // 换牌2次
        for (j in 0..1) {
            for (i in 0..length - 1) {
                val value = tempCard[i]
                val randomIndex = random.nextInt(length)
                tempCard[i] = tempCard[randomIndex]
                tempCard[randomIndex] = value
            }
        }
        return tempCard.toCollection(ArrayList<Int>())
    }

    fun totalCardCount(): Int {
        var size = if (includeKing) 54 else 52
        return size * cardCount
    }
}