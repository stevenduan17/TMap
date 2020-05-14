package com.steven.tmap.algorithm

import kotlin.random.Random

/**
 * 基因遗传交叉算法
 *
 * @author Steven
 * @version 1.0
 * @since 2020/5/13
 */
internal class Genetic {

    companion object {
        private var instance: Genetic? = null

        /**
         *  默认种群数量
         */
        private const val POPULATION_SIZE = 30

        /**
         * 默认交叉概率
         */
        private const val CROSSOVER_PROBABILITY = 0.9f

        /**
         * 默认突变概率
         */
        private const val MUTATION_PROBABILITY = 0.01f

        /**
         * 默认最大代
         */
        private const val DEFAULT_MAX_GENERATION = 1000

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: Genetic().also { instance = it }
        }
    }

    /**
     * 变异次数
     */
    private var mutationTimes = 0

    /**
     * 当前代
     */
    private var currentGeneration = 0

    private var maxGeneration = DEFAULT_MAX_GENERATION

    /**
     * 最短结果集
     */
    private var bestIndividual: IntArray? = null

    /**
     * 最短距离
     */
    private var shortestDistance = 0f

    /**
     * 当前最好个体位置
     */
    private var currentBestPosition = 0

    /**
     * 当前最好个体距离
     */
    private var currentBestDistance = 0f

    /**
     *  点集间的邻接矩阵
     */
    private lateinit var dist: Array<FloatArray>

    private var pointSize = 0

    /**
     * 种群集
     */
    private lateinit var population: Array<IntArray>

    /**
     * 种群中每个个体的邻接矩阵
     */
    private lateinit var values: FloatArray

    /**
     * 适应度集
     */
    private lateinit var fitnessValues: FloatArray

    private lateinit var roulette: FloatArray

    var autoNextGeneration = false

    fun tsp(matrix: Array<FloatArray>): IntArray {
        this.dist = matrix
        this.pointSize = matrix.size
        init()
        if (autoNextGeneration) {
            var i = 0
            while (i++ < maxGeneration) {
                nextGeneration()
            }
        }
        autoNextGeneration = false
        return getBestIndividual()
    }

    fun setMaxGeneration(maxGeneration: Int) {
        this.maxGeneration = maxGeneration
    }

    private fun init() {
        this.mutationTimes = 0
        this.currentGeneration = 0
        this.bestIndividual = null
        this.shortestDistance = 0f
        this.currentBestPosition = 0
        this.currentBestDistance = 0f
        this.values = FloatArray(POPULATION_SIZE)
        this.fitnessValues = FloatArray(POPULATION_SIZE)
        this.roulette = FloatArray(POPULATION_SIZE)
        this.population = Array(POPULATION_SIZE) { IntArray(pointSize) }
        for (i in 0 until POPULATION_SIZE) {
            this.population[i] = randomIndividual(pointSize)
        }
        evaluateBestIndividual()
    }

    private fun nextGeneration() {
        currentGeneration++
        //选择
        selection()
        //交叉
        crossover()
        //变异
        mutation()
        //评估最好
        evaluateBestIndividual()
        //  getBestIndividual()
    }

    private fun getBestIndividual(): IntArray {
        val best = IntArray(bestIndividual!!.size)
        val position = indexOf(bestIndividual!!, 0)
        for (i in best.indices) {
            best[i] = bestIndividual!![(i + position) % bestIndividual!!.size]
        }
        return best
    }

    private fun indexOf(a: IntArray, index: Int): Int {
        for (i in a.indices) {
            if (a[i] == index) return i
        }
        return 0
    }

    /**
     * 产生乱序个体
     */
    private fun randomIndividual(size: Int): IntArray {
        val array = IntArray(size)
        for (i in 0 until size) {
            array[i] = i
        }
        val list = array.toMutableList()
        list.shuffle()
        return list.toIntArray()
    }

    private fun selection() {
        val parents = Array(POPULATION_SIZE) { IntArray(pointSize) }
        parents[0] = population[currentBestPosition]
        parents[1] = exchangeMutate(bestIndividual!!.clone())
        parents[2] = insertMutate(bestIndividual!!.clone())
        parents[3] = bestIndividual!!.clone()

        setRoulette()

        for (i in 4 until POPULATION_SIZE) {
            parents[1] = population[wheelOut()]
        }

        population = parents
    }

    private fun crossover() {
        var queue = IntArray(POPULATION_SIZE)
        var num = 0
        for (i in queue.indices) {
            if (Random.nextFloat() < CROSSOVER_PROBABILITY) {
                queue[num] = i
                num++
            }
        }
        queue = queue.copyOfRange(0, num)
        for (i in 0 until num - 1 step 2) {
            doCrossover(queue[i], queue[i + 1])
        }
    }

    private fun mutation() {
        var i = 0
        while (i < POPULATION_SIZE) {
            if (Math.random() < MUTATION_PROBABILITY) {
                if (Math.random() > 0.5f) {
                    population[i] = insertMutate(population[i])
                } else {
                    population[i] = exchangeMutate(population[i])
                }
                i--
            }
            i++
        }
    }

    private fun evaluateBestIndividual() {
        for (i in population.indices) {
            values[i] = calculateIndividualDistance(population[i])
        }
        evaluateBestCurrentDistance()
        if (shortestDistance == 0f || shortestDistance > currentBestDistance) {
            shortestDistance = currentBestDistance
            bestIndividual = population[currentBestPosition].clone()
        }
    }

    private fun exchangeMutate(seq: IntArray): IntArray {
        mutationTimes++
        var m: Int
        var n: Int
        do {
            m = Random.nextInt(seq.size - 2)
            n = Random.nextInt(seq.size)

        } while (m >= n)

        val j = (n - m + 1) shr 1
        for (i in 0 until j) {
            val tmp = seq[m + i]
            seq[m + i] = seq[n - i]
            seq[n - i] = tmp
        }
        return seq
    }

    private fun insertMutate(seq: IntArray): IntArray {
        mutationTimes++
        var m: Int
        var n: Int
        do {
            m = Random.nextInt(seq.size shr 1)
            n = Random.nextInt(seq.size)
        } while (m >= n)

        val s1 = seq.copyOfRange(0, m)
        val s2 = seq.copyOfRange(m, n)

        for (i in 0 until m) {
            seq[i + n - m] = s1[i]
        }

        for (i in 0 until n - m) {
            seq[i] = s2[i]
        }
        return seq
    }

    private fun setRoulette() {
        //计算适应度
        for (i in values.indices) {
            fitnessValues[i] = 1f / values[i]
        }
        var sum = 0f
        fitnessValues.forEach { sum += it }
        for (i in roulette.indices) {
            roulette[i] = fitnessValues[i] / sum
        }
        for (i in roulette.indices) {
            roulette[i] += roulette[i - 1]
        }
    }

    /**
     * 模拟转盘  进行子代选取
     */
    private fun wheelOut(): Int {
        val r = Random.nextFloat()
        for (i in roulette.indices) {
            if (r <= roulette[i]) return i
        }
        return 0
    }

    private fun doCrossover(x: Int, y: Int) {
        population[x] = getChild(x, y, 0)
        population[y] = getChild(x, y, 1)
    }

    private fun getChild(x: Int, y: Int, preOrNext: Int): IntArray {
        val solution = IntArray(pointSize)
        var px = population[x].clone()
        var py = population[y].clone()
        var dx = 0
        var dy = 0
        var c = px[Random.nextInt(px.size)]
        solution[0] = c

        for (i in 1 until pointSize) {
            val posX = indexOf(px, c)
            val posY = indexOf(py, c)
            if (preOrNext == 0) {
                dx = px[(posX + px.size - 1) % px.size]
                dy = py[(posY + py.size - 1) % py.size]
            } else if (preOrNext == 1) {
                dx = px[(posX + px.size + 1) % px.size]
                dy = py[(posY + py.size + 1) % py.size]
            }
            for (j in posX until px.size - 1) {
                px[j] = px[j + 1]
            }
            px = px.copyOfRange(0, px.size - 1)
            for (j in posY until py.size - 1) {
                py[j] = py[j + 1]
            }
            py = py.copyOfRange(0, py.size - 1)
            c = if (dist[c][dx] < dist[c][dy]) dx else dy
            solution[i] = c
        }

        return solution
    }

    /**
     * 计算个体距离
     */
    private fun calculateIndividualDistance(individual: IntArray): Float {
        var sum = dist[individual[0]][individual[individual.size - 1]]
        for (i in 1 until individual.size) {
            sum += dist[individual[i]][individual[i - 1]]
        }
        return sum
    }

    /**
     * 评估得到最短距离
     */
    private fun evaluateBestCurrentDistance() {
        currentBestDistance = values[0]
        for (i in 1 until POPULATION_SIZE) {
            if (values[i] < currentBestDistance) {
                currentBestDistance = values[i]
                currentBestPosition = i
            }
        }
    }
}