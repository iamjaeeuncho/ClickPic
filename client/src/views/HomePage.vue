<template>
  <div>
    <CarouselComponent />
    <HotComponent />
    <FooterComponent />
  </div>
</template>

<script>
import CarouselComponent from '@/components/main/CarouselComponent.vue';
import HotComponent from '@/components/main/HotComponent.vue';

export default {
  name: 'HomePage',
  components: {
    CarouselComponent,
    HotComponent,
  },
  mounted() {
    const storedToken = localStorage.getItem('userId');
    console.log(this.$store.state.userId);
    // Vuex에 accessToken이 없고 로컬 스토리지에 저장된 토큰이 있다면 갱신
    if (storedToken && !this.$store.state.userId) {
      window.location.reload();
      this.$store.dispatch('fetchUserInfo'); // Vuex 상태 갱신
    }
  }
};
</script>

<style scoped>
</style>
