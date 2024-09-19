<template>
  <div id="app">
    <HeaderComponent />
    <ContainerComponent />
    <FooterComponent />
  </div>
</template>

<script>
import HeaderComponent from '@/components/HeaderComponent.vue';
import FooterComponent from '@/components/FooterComponent.vue';
import ContainerComponent from '@/components/ContainerComponent.vue';

export default {
  name: 'App',
  components: {
    HeaderComponent,
    ContainerComponent,
    FooterComponent,
  },
  created() {
    const storedToken = localStorage.getItem('accessToken');
    
    if (storedToken) {
      this.$store.commit('setAccessToken', storedToken); // Vuex에 토큰 갱신
      this.$store.dispatch('fetchUserInfo');
    }
  }
};
</script>

<style>
html, body {
  background-color: #EEEDEE;
  color: #101010;
  height: 100%;
  padding: 0;
  margin: 0;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.content {
  flex: 1;
  box-sizing: border-box;
}
</style>