<template>
    <div></div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    computed: {
      userInfo() {
        return this.$store.state.userInfo;
      }
    },
    methods: {
      fetchUserInfo() {
        const storedToken = localStorage.getItem('accessToken');
        this.accessToken = new URLSearchParams(window.location.search).get('accessToken') || storedToken;
  
        if (!this.accessToken) {
          this.$router.push('/');
          return;
        }
  
        axios.get(`${process.env.VUE_APP_BACKEND_URL}/api/userinfo`, {
        headers: {
            Authorization: `Bearer ${this.accessToken}`,
        },
        withCredentials: true,
        })
        .then(response => {
          // Vuex 상태 업데이트
          this.$store.commit('setUserInfo', response.data);
  
          // 로컬 스토리지에 사용자 정보를 저장
          localStorage.setItem('userId', response.data.id);
          localStorage.setItem('nickname', response.data.properties.nickname);
          localStorage.setItem('accessToken', this.accessToken);
          console.log(response.data);
        })
        .catch(error => {
          console.error('사용자 정보 불러오기 실패:', error);
        });
      },
      logout() {
        axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/logout`, new URLSearchParams({ accessToken: this.accessToken }))
        .then(() => {
          this.$store.commit('logout'); // Vuex의 logout mutation 호출
          this.$router.push('/'); // 로그아웃 후 메인페이지로 이동
        })
        .catch(error => {
          console.error('로그아웃 실패:', error);
        });
      }
    },
    mounted() {
  this.fetchUserInfo();
  this.$router.push('/'); // 메인 페이지로 이동
}
  }
  </script>
  
  <style scoped>
  .logout-button {
    margin-top: 20px;
    padding: 10px 20px;
    background-color: red;
    color: white;
    border: none;
    cursor: pointer;
  }
  </style>
  