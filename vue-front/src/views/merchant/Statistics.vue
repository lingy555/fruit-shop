
<template>
  <div class="statistics-container">
    <div class="statistics-header">
      <h2>数据统计</h2>

      <!-- 日期选择 -->
      <div class="date-picker">
        <el-radio-group v-model="dateType" @change="fetchStatistics">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="year">本年</el-radio-button>
          <el-radio-button value="custom">自定义</el-radio-button>
        </el-radio-group>

        <div class="custom-date" v-if="dateType === 'custom'">
          <el-date-picker
            v-model="customDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchStatistics"
          />
        </div>
      </div>
    </div>

    <div class="statistics-content" v-loading="loading">
      <!-- 概览卡片 -->
      <div class="overview-cards">
        <div class="card">
          <div class="card-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">订单数</div>
            <div class="card-value">{{ overviewData.orderCount || 0 }}</div>
            <div class="card-compare" v-if="overviewData.orderCompareRate">
              <span :class="{ 'up': overviewData.orderCompareRate > 0, 'down': overviewData.orderCompareRate < 0 }">
                {{ Math.abs(overviewData.orderCompareRate) }}%
                <el-icon v-if="overviewData.orderCompareRate > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
              </span>
              <span class="compare-label">较昨日</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">成交额</div>
            <div class="card-value">¥{{ overviewData.orderAmount || 0 }}</div>
            <div class="card-compare" v-if="overviewData.amountCompareRate">
              <span :class="{ 'up': overviewData.amountCompareRate > 0, 'down': overviewData.amountCompareRate < 0 }">
                {{ Math.abs(overviewData.amountCompareRate) }}%
                <el-icon v-if="overviewData.amountCompareRate > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
              </span>
              <span class="compare-label">较昨日</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">访问量</div>
            <div class="card-value">{{ overviewData.visitCount || 0 }}</div>
            <div class="card-compare" v-if="overviewData.visitCompareRate">
              <span :class="{ 'up': overviewData.visitCompareRate > 0, 'down': overviewData.visitCompareRate < 0 }">
                {{ Math.abs(overviewData.visitCompareRate) }}%
                <el-icon v-if="overviewData.visitCompareRate > 0"><CaretTop /></el-icon>
                <el-icon v-else><CaretBottom /></el-icon>
              </span>
              <span class="compare-label">较昨日</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">新增粉丝</div>
            <div class="card-value">{{ overviewData.newFanCount || 0 }}</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 销售趋势图 -->
        <div class="chart-card">
          <div class="card-header">
            <h3>销售趋势</h3>
            <div class="chart-actions">
              <el-radio-group v-model="chartType" @change="fetchChartData">
                <el-radio-button value="amount">成交额</el-radio-button>
                <el-radio-button value="count">订单数</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div class="chart-container">
            <div ref="salesChart" class="chart"></div>
          </div>
        </div>

        <!-- 分类占比图 -->
        <div class="chart-card">
          <div class="card-header">
            <h3>分类占比</h3>
          </div>

          <div class="chart-container">
            <div ref="categoryChart" class="chart"></div>
          </div>
        </div>

        <!-- 商品排行 -->
        <div class="chart-card">
          <div class="card-header">
            <h3>商品排行</h3>
            <div class="chart-actions">
              <el-radio-group v-model="productRankType" @change="fetchProductRank">
                <el-radio-button value="sales">销量</el-radio-button>
                <el-radio-button value="amount">销售额</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div class="table-container">
            <el-table
              :data="productRankData"
              style="width: 100%"
              :row-class-name="getRowClassName"
            >
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="productName" label="商品名称" />
              <el-table-column prop="sales" label="销量" width="100" />
              <el-table-column prop="amount" label="销售额" width="120">
                <template #default="{ row }">
                  ¥{{ formatMoney(row.amount) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { merchantStatistics } from '@/api'
import * as echarts from 'echarts'

const toNumber = (val) => {
  if (val === null || val === undefined || val === '') return 0
  const n = Number(val)
  return Number.isFinite(n) ? n : 0
}

const formatMoney = (val) => {
  const n = toNumber(val)
  return n.toFixed(2)
}

let salesChartInstance = null
let categoryChartInstance = null

const resizeCharts = () => {
  salesChartInstance?.resize()
  categoryChartInstance?.resize()
}

const waitForNonZeroSize = (el, cb, retries = 30) => {
  if (!el) return
  const w = el.clientWidth
  const h = el.clientHeight
  if (w > 0 && h > 0) {
    cb()
    return
  }
  if (retries <= 0) {
    cb()
    return
  }
  requestAnimationFrame(() => waitForNonZeroSize(el, cb, retries - 1))
}

// 日期类型
const dateType = ref('today')

// 自定义日期范围
const customDateRange = ref([])

// 概览数据
const overviewData = ref({})

// 商品排行数据
const productRankData = ref([])

const salesChart = ref(null)
const categoryChart = ref(null)

// 图表类型
const chartType = ref('amount')

// 商品排行类型
const productRankType = ref('sales')

// 加载状态
const loading = ref(false)

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    let params = { dateType: dateType.value }

    // 如果是自定义日期，添加日期范围
    if (dateType.value === 'custom' && customDateRange.value.length === 2) {
      const [startDate, endDate] = customDateRange.value
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }

    const response = await merchantStatistics.overview(params)
    overviewData.value = response.data.todayData || {}

    // 获取图表数据
    await fetchChartData()
    await fetchCategoryData()
    await fetchProductRank()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取图表数据
const fetchChartData = async () => {
  try {
    let params = { dateType: dateType.value }

    // 如果是自定义日期，添加日期范围
    if (dateType.value === 'custom' && customDateRange.value.length === 2) {
      const [startDate, endDate] = customDateRange.value
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }

    const response = await merchantStatistics.sales(params)
    renderSalesChart(response.data.trendData || [])
  } catch (error) {
    console.error('获取图表数据失败:', error)
  }
}

// 获取分类数据
const fetchCategoryData = async () => {
  try {
    let params = { dateType: dateType.value }

    // 如果是自定义日期，添加日期范围
    if (dateType.value === 'custom' && customDateRange.value.length === 2) {
      const [startDate, endDate] = customDateRange.value
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }

    const response = await merchantStatistics.sales(params)
    // 尝试多种可能的数据结构
    let data = response.data?.categoryData || response.data?.category || []
    // 如果还是没有数据，尝试从主数据中提取
    if (!data || data.length === 0) {
      data = response.data?.trendData || []
    }
    console.log('Category data:', data)
    renderCategoryChart(data)
  } catch (error) {
    console.error('获取分类数据失败:', error)
    // 渲染空图表
    renderCategoryChart([])
  }
}

// 获取商品排行
const fetchProductRank = async () => {
  try {
    let params = { 
      dateType: dateType.value, 
      sortBy: productRankType.value 
    }

    // 如果是自定义日期，添加日期范围
    if (dateType.value === 'custom' && customDateRange.value.length === 2) {
      const [startDate, endDate] = customDateRange.value
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }

    const response = await merchantStatistics.product(params)
    const list = response?.data?.topSalesProducts || []
    const sorted = [...list].sort((a, b) => {
      if (productRankType.value === 'amount') {
        return toNumber(b?.amount) - toNumber(a?.amount)
      }
      return toNumber(b?.sales) - toNumber(a?.sales)
    })
    productRankData.value = sorted
  } catch (error) {
    console.error('获取商品排行失败:', error)
  }
}

// 渲染销售趋势图
const renderSalesChart = (data) => {
  nextTick(() => {
    if (!salesChart.value) return
    waitForNonZeroSize(salesChart.value, () => {
      if (salesChartInstance) {
        salesChartInstance.dispose()
      }
      salesChartInstance = echarts.init(salesChart.value)

      const option = {
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date || '')
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          type: 'line',
          data: data.map(item => {
            if (chartType.value === 'amount') {
              return item.orderAmount || 0
            }
            return item.orderCount || 0
          }),
          smooth: true,
          itemStyle: { color: '#4CAF50' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(76, 175, 80, 0.3)' },
                { offset: 1, color: 'rgba(76, 175, 80, 0.1)' }
              ]
            }
          }
        }]
      }

      salesChartInstance.setOption(option)
      requestAnimationFrame(() => salesChartInstance?.resize())
    })
  })
}

// 渲染分类占比图
const renderCategoryChart = (data) => {
  console.log('renderCategoryChart data:', data)
  nextTick(() => {
    if (!categoryChart.value) return
    waitForNonZeroSize(categoryChart.value, () => {
      if (categoryChartInstance) {
        categoryChartInstance.dispose()
      }
      categoryChartInstance = echarts.init(categoryChart.value)

      // 如果没有数据，显示占位提示
      if (!data || data.length === 0) {
        const option = {
          title: {
            text: '暂无数据',
            left: 'center',
            top: 'middle',
            textStyle: {
              color: '#999'
            }
          }
        }
        categoryChartInstance.setOption(option)
        return
      }

      // 打印数据结构用于调试
      console.log('Category data structure:', data[0])

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: (params) => {
            return `${params.name}: ¥${params.value} (${params.percent}%)`
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '分类占比',
            type: 'pie',
            radius: '50%',
            data: data.map(item => ({
              value: item.amount || item.orderAmount || 0,
              name: item.categoryName || item.category || '未知分类'
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              show: true,
              formatter: '{b}: {d}%'
            }
          }
        ]
      }

      categoryChartInstance.setOption(option)
      requestAnimationFrame(() => categoryChartInstance?.resize())
    })
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''

  if (date instanceof Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
  if (typeof date === 'string') {
    return date.length >= 10 ? date.slice(0, 10) : date
  }
  const d = new Date(date)
  if (Number.isNaN(d.getTime())) {
    return String(date)
  }
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 表格行样式
const getRowClassName = ({ row, rowIndex }) => {
  if (rowIndex < 3) {
    return 'top-row'
  }
  return ''
}

// 初始化
onMounted(() => {
  fetchStatistics()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  salesChartInstance?.dispose()
  categoryChartInstance?.dispose()
  salesChartInstance = null
  categoryChartInstance = null
})
</script>

<style scoped>
.statistics-container {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
}

.statistics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.statistics-header h2 {
  font-size: 20px;
  color: #333;
}

.date-picker {
  display: flex;
  align-items: center;
  gap: 20px;
}

.custom-date {
  margin-left: 10px;
}

.statistics-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #4CAF50;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 5px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.card-compare {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.up {
  color: #4CAF50;
}

.down {
  color: #f56c6c;
}

.compare-label {
  margin-left: 5px;
  color: #999;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  font-size: 18px;
  color: #333;
}

.chart-actions {
  display: flex;
  gap: 10px;
}

.chart-container {
  height: 300px;
  min-height: 300px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

.table-container {
  margin-top: 15px;
}

:deep(.el-table .top-row) {
  background-color: rgba(76, 175, 80, 0.05);
}

:deep(.el-table .cell) {
  padding: 8px 0;
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>
