<template>
  <div class="product-card skeleton">
    <div class="skeleton-image shimmer"></div>
    <div class="skeleton-info">
      <div class="skeleton-line title shimmer"></div>
      <div class="skeleton-line subtitle shimmer"></div>
      <div class="skeleton-price shimmer"></div>
      <div class="skeleton-meta">
        <div class="skeleton-chip shimmer"></div>
        <div class="skeleton-chip shimmer"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 2px 12px rgba(15, 118, 110, 0.08);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-image {
  height: 190px;
  border-radius: 14px;
  background: #f2f5f7;
}

.skeleton-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 14px;
  border-radius: 999px;
  background: #eef2f7;
}

.skeleton-line.title {
  width: 80%;
}

.skeleton-line.subtitle {
  width: 60%;
}

.skeleton-price {
  height: 18px;
  width: 40%;
  border-radius: 999px;
  background: #ffe3d5;
}

.skeleton-meta {
  display: flex;
  gap: 10px;
}

.skeleton-chip {
  height: 20px;
  flex: 1;
  border-radius: 999px;
  background: #e8f5e9;
}

.shimmer {
  position: relative;
  overflow: hidden;
}

.shimmer::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(120deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  animation: shimmer 1.6s infinite;
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}
</style>
