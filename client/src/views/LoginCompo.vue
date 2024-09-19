<template>
  <div class="login-container">
    <div class="largeTitle">LETS'S GET STARTED!</div>
    
    <!-- 일반 로그인 버튼 -->
    <button @click="goToGeneralLogin" class="general-login-button">
      일반 로그인
    </button>

    <!-- 카카오 로그인 버튼 -->
    <button v-if="!accessToken" @click="loginWithKakao" class="kakao-login-button"></button>
    
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      accessToken: null
    };
  },
  methods: {
    loginWithKakao() {
      const clientId = 'fa48f38c035cc445070338897bcbb504';
      const redirectUri = `${process.env.VUE_APP_BACKEND_URL}/api/kakao/callback`;
      const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&prompt=login`;

      console.log("Kakao 로그인 페이지로 이동:", kakaoAuthUrl);
      window.location.href = kakaoAuthUrl;
    },
    goToGeneralLogin() {
      // 일반 로그인 페이지로 라우터를 통해 이동
      this.$router.push('/generallogin');
    },
    checkLogin() {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');

      if (code) {
        axios.get(`${process.env.VUE_APP_BACKEND_URL}/api/kakao/callback?code=${code}`)
          .then(response => {
            this.accessToken = response.data.accessToken;
            if (this.accessToken) {
              localStorage.setItem('accessToken', this.accessToken);
              this.$router.push('/profile');
            } else {
              console.error("로그인 실패: accessToken이 없음");
            }
          })
          .catch(error => {
            console.error('로그인 실패:', error);
          });
      }
    }
  },
  mounted() {
    const storedToken = localStorage.getItem('accessToken');
    if (storedToken) {
      this.accessToken = storedToken;
      this.$router.push('/profile');
    } else {
      this.checkLogin();
    }
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.kakao-login-button {
  width: 500px;
  height: 75px;
  background-image: url('@/assets/kakao_login_large_wide.png');
  background-size: cover;
  background-repeat: no-repeat;
  border: none;
  cursor: pointer;
  margin-bottom: 20px;
}

.general-login-button {
  width: 500px;
  height: 75px;
  background-color: #ffffff;
  color: #DB574D;
  font-size: 20px;
  border: 1px solid #DB574D;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 20px;
}

.general-login-button:hover {
  background-color: #DB574D;
  color: white;
}
</style>
