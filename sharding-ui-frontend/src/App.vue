<template>
  <div id="app">
    <router-view/>
    <Footer/>
  </div>
</template>

<script>
import Footer from '@/views/footer/index'
export default {
  name: 'App',
  components: { Footer }
}
</script>
