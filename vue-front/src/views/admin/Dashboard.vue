
<template>
  <div class="dashboard-container">
    <!-- 数据概览 -->
    <div class="overview-section">
      <h2 class="section-title">数据概览</h2>

      <div class="overview-cards">
        <!-- 今日数据 -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">今日数据</span>
            <el-date-picker
              v-model="todayDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchTodayData"
            />
          </div>

          <div class="card-content">
            <div class="data-item">
              <div class="data-label">订单数</div>
              <div class="data-value">{{ todayData.orderCount || 0 }}</div>
              <div class="data-compare" v-if="todayData.orderCompareRate">
                <span :class="{ 'up': todayData.orderCompareRate > 0, 'down': todayData.orderCompareRate < 0 }">
                  {{ Math.abs(todayData.orderCompareRate) }}%
                  <el-icon v-if="todayData.orderCompareRate > 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                </span>
                <span class="compare-label">较昨日</span>
              </div>
            </div>

            <div class="data-item">
              <div class="data-label">成交额</div>
              <div class="data-value">¥{{ todayData.orderAmount || 0 }}</div>
              <div class="data-compare" v-if="todayData.amountCompareRate">
                <span :class="{ 'up': todayData.amountCompareRate > 0, 'down': todayData.amountCompareRate < 0 }">
                  {{ Math.abs(todayData.amountCompareRate) }}%
                  <el-icon v-if="todayData.amountCompareRate > 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                </span>
                <span class="compare-label">较昨日</span>
              </div>
            </div>

            <div class="data-item">
              <div class="data-label">访问量</div>
              <div class="data-value">{{ todayData.visitCount || 0 }}</div>
              <div class="data-compare" v-if="todayData.visitCompareRate">
                <span :class="{ 'up': todayData.visitCompareRate > 0, 'down': todayData.visitCompareRate < 0 }">
                  {{ Math.abs(todayData.visitCompareRate) }}%
                  <el-icon v-if="todayData.visitCompareRate > 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                </span>
                <span class="compare-label">较昨日</span>
              </div>
            </div>

            <div class="data-item">
              <div class="data-label">新增用户</div>
              <div class="data-value">{{ todayData.newUserCount || 0 }}</div>
              <div class="data-compare" v-if="todayData.userCompareRate">
                <span :class="{ 'up': todayData.userCompareRate > 0, 'down': todayData.userCompareRate < 0 }">
                  {{ Math.abs(todayData.userCompareRate) }}%
                  <el-icon v-if="todayData.userCompareRate > 0"><CaretTop /></el-icon>
                  <el-icon v-else><CaretBottom /></el-icon>
                </span>
                <span class="compare-label">较昨日</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 平台统计 -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">平台统计</span>
          </div>

          <div class="card-content">
            <div class="data-item">
              <div class="data-label">总用户数</div>
              <div class="data-value">{{ platformData.totalUserCount || 0 }}</div>
            </div>

            <div class="data-item">
              <div class="data-label">总商家数</div>
              <div class="data-value">{{ platformData.totalMerchantCount || 0 }}</div>
            </div>

            <div class="data-item">
              <div class="data-label">总商品数</div>
              <div class="data-value">{{ platformData.totalProductCount || 0 }}</div>
            </div>

            <div class="data-item">
              <div class="data-label">总订单数</div>
              <div class="data-value">{{ platformData.totalOrderCount || 0 }}</div>
            </div>

            <div class="data-item">
              <div class="data-label">总成交额</div>
              <div class="data-value">¥{{ platformData.totalOrderAmount || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="section-header">
        <h2 class="section-title">数据图表</h2>

        <div class="chart-controls">
          <el-radio-group v-model="chartType" @change="fetchChartData">
            <el-radio-button value="order">订单趋势</el-radio-button>
            <el-radio-button value="user">用户增长</el-radio-button>
            <el-radio-button value="merchant">商家增长</el-radio-button>
            <el-radio-button value="product">商品分析</el-radio-button>
          </el-radio-group>

          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="fetchChartData"
          />
        </div>
      </div>

      <div class="chart-container">
        <div ref="chartDom" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { admin } from '@/api'
import * as echarts from 'echarts'

// 今日日期
const todayDate = ref(new Date().toISOString().split('T')[0])

// 今日数据
const todayData = ref({})

// 平台数据
const platformData = ref({})

// 图表类型
const chartType = ref('order')

// 日期范围
const dateRange = ref([])

const chartDom = ref(null)
let chartInstance = null

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 加载状态
const loading = ref(false)

// 获取今日数据
const fetchTodayData = async () => {
  loading.value = true
  try {
    const date = todayDate.value instanceof Date ? todayDate.value.toISOString().split('T')[0] : todayDate.value
    const response = await admin.todayData({ date })
    todayData.value = response.data || {}
  } catch (error) {
    console.error('获取今日数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取平台数据
const fetchPlatformData = async () => {
  loading.value = true
  try {
    const response = await admin.platformData()
    platformData.value = response.data || {}
  } catch (error) {
    console.error('获取平台数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取图表数据
const fetchChartData = async () => {
  loading.value = true
  try {
    let params = {}

    // 如果是自定义日期范围，添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      const [startDate, endDate] = dateRange.value
      params.startDate = startDate
      params.endDate = endDate
    }

    const response = await admin.chartData({ 
      type: chartType.value,
      ...params
    })

    // 根据图表类型渲染不同的图表
    renderChart(response.data || [])
  } catch (error) {
    console.error('获取图表数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderChart = (data) => {
  nextTick(() => {
    if (!chartDom.value) return

    if (!chartInstance) {
      chartInstance = echarts.init(chartDom.value)
    } else if (chartInstance.getDom && chartInstance.getDom() !== chartDom.value) {
      chartInstance.dispose()
      chartInstance = echarts.init(chartDom.value)
    }

    let option = {}

    switch (chartType.value) {
      case 'order':
        option = {
          title: { text: '订单趋势' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['订单数', '成交额'] },
          grid: { left: '3%', right: '4%' },
          xAxis: { type: 'category', boundaryGap: false },
          yAxis: [
            { type: 'value', name: '订单数', min: 0, position: 'left' },
            { type: 'value', name: '成交额', min: 0, position: 'right' }
          ],
          series: [
            {
              name: '订单数',
              type: 'line',
              smooth: true,
              data: data.map(item => ({ name: item.date, value: item.orderCount })),
              itemStyle: { color: '#4CAF50' }
            },
            {
              name: '成交额',
              type: 'line',
              smooth: true,
              data: data.map(item => ({ name: item.date, value: item.orderAmount })),
              itemStyle: { color: '#ff9900' }
            }
          ]
        }
        break
      case 'user':
        option = {
          title: { text: '用户增长' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['新增用户', '累计用户'] },
          grid: { left: '3%', right: '4%' },
          xAxis: { type: 'category', boundaryGap: false },
          yAxis: [
            { type: 'value', name: '新增用户', min: 0, position: 'left' },
            { type: 'value', name: '累计用户', min: 0, position: 'right' }
          ],
          series: [
            {
              name: '新增用户',
              type: 'bar',
              stack: 'user',
              data: data.map(item => ({ name: item.date, value: item.newUserCount })),
              itemStyle: { color: '#4CAF50' }
            },
            {
              name: '累计用户',
              type: 'line',
              smooth: true,
              data: data.map(item => ({ name: item.date, value: item.totalUserCount })),
              itemStyle: { color: '#ff9900' }
            }
          ]
        }
        break
      case 'merchant':
        option = {
          title: { text: '商家增长' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['新增商家', '累计商家'] },
          grid: { left: '3%', right: '4%' },
          xAxis: { type: 'category', boundaryGap: false },
          yAxis: [
            { type: 'value', name: '新增商家', min: 0, position: 'left' },
            { type: 'value', name: '累计商家', min: 0, position: 'right' }
          ],
          series: [
            {
              name: '新增商家',
              type: 'bar',
              stack: 'merchant',
              data: data.map(item => ({ name: item.date, value: item.newMerchantCount })),
              itemStyle: { color: '#4CAF50' }
            },
            {
              name: '累计商家',
              type: 'line',
              smooth: true,
              data: data.map(item => ({ name: item.date, value: item.totalMerchantCount })),
              itemStyle: { color: '#ff9900' }
            }
          ]
        }
        break
      case 'product':
        option = {
          title: { text: '商品分析' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['商品数量', '商品销量'] },
          grid: { left: '3%', right: '4%' },
          xAxis: { type: 'category', boundaryGap: false },
          yAxis: [
            { type: 'value', name: '商品数量', min: 0, position: 'left' },
            { type: 'value', name: '商品销量', min: 0, position: 'right' }
          ],
          series: [
            {
              name: '商品数量',
              type: 'bar',
              data: data.map(item => ({ name: item.categoryName, value: item.productCount })),
              itemStyle: { color: '#4CAF50' }
            },
            {
              name: '商品销量',
              type: 'bar',
              data: data.map(item => ({ name: item.categoryName, value: item.productSales })),
              itemStyle: { color: '#ff9900' }
            }
          ]
        }
        break
    }

    chartInstance.setOption(option)
  })
}

// 初始化
onMounted(() => {
  fetchTodayData()
  fetchPlatformData()
  fetchChartData()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.overview-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 500;
}

.overview-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  flex: 1;
  min-width: 280px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.data-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.data-label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}

.data-value {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.data-compare {
  display: flex;
  align-items: center;
  gap: 5px;
}

.up {
  color: #4CAF50;
}

.down {
  color: #ff5722;
}

.compare-label {
  font-size: 12px;
  color: #999;
}

.charts-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.chart-container {
  height: 400px;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .overview-cards {
    flex-direction: column;
  }

  .card {
    min-width: auto;
  }
}
</style>
