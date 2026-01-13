const isNativeLazySupported = typeof window !== 'undefined' && 'loading' in HTMLImageElement.prototype

const loadImage = (el, src) => {
  if (!src) return
  el.src = src
}

const addLoadedClass = (el) => {
  const handleLoad = () => {
    el.classList.add('is-loaded')
    el.removeEventListener('load', handleLoad)
  }

  el.addEventListener('load', handleLoad)
}

export default {
  mounted(el, binding) {
    el.__lazyValue = binding.value
    addLoadedClass(el)

    if (isNativeLazySupported) {
      el.setAttribute('loading', 'lazy')
      loadImage(el, el.__lazyValue)
      el.__lazyLoaded = true
      return
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          loadImage(el, el.__lazyValue)
          el.__lazyLoaded = true
          observer.unobserve(el)
        }
      })
    }, {
      rootMargin: '50px',
      threshold: 0.01
    })

    observer.observe(el)
    el.__lazyObserver = observer
  },

  updated(el, binding) {
    if (binding.value === binding.oldValue) return
    el.__lazyValue = binding.value
    if (el.__lazyLoaded || isNativeLazySupported) {
      loadImage(el, el.__lazyValue)
    }
  },

  unmounted(el) {
    el.__lazyObserver?.disconnect()
    delete el.__lazyObserver
  }
}
