import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store'; // Vuex store import

new Vue({
  router,
  store, // Vuex store 등록
  render: h => h(App)
}).$mount('#app');
