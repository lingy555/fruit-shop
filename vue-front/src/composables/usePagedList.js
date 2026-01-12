import { ref, reactive } from 'vue'

export function usePagedList(fetchApi, options = {}) {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)

  const pagination = reactive({
    page: options.page ?? 1,
    pageSize: options.pageSize ?? 10,
    total: 0
  })

  const query = reactive({ ...(options.query || {}) })

  const fetch = async (extraQuery) => {
    loading.value = true
    try {
      const params = {
        ...query,
        ...(extraQuery || {}),
        page: pagination.page,
        pageSize: pagination.pageSize
      }
      const res = await fetchApi(params)
      list.value = res?.data?.list || []
      total.value = res?.data?.total || 0
      pagination.total = total.value

      if (typeof options.onSuccess === 'function') {
        options.onSuccess(res)
      }

      return res
    } finally {
      loading.value = false
    }
  }

  const onSizeChange = (size, extraQuery) => {
    pagination.pageSize = size
    pagination.page = 1
    return fetch(extraQuery)
  }

  const onCurrentChange = (page, extraQuery) => {
    pagination.page = page
    return fetch(extraQuery)
  }

  const resetPage = () => {
    pagination.page = 1
  }

  return {
    list,
    total,
    loading,
    pagination,
    query,
    fetch,
    onSizeChange,
    onCurrentChange,
    resetPage
  }
}
