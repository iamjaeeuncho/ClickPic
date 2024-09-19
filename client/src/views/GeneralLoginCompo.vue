<template>
    <div class="login-container">
      <h1>로그인</h1>
      <form @submit.prevent="loginUser">
        <div>
          <label for="userId">아이디:</label>
          <input type="text" id="userId" v-model="userId" required />
        </div>
        <div>
          <label for="password">비밀번호:</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <button type="submit" class="btn-large">로그인</button>
      </form>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        userId: '',
        password: ''
      };
    },
    methods: {
        loginUser() {
            axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/login`, {
                userId: this.userId,
                password: this.password
            })
            .then(response => {
                console.log('API Response:', response.data); // 응답 데이터 확인
                console.log(response); 
                // userId와 nickname을 로컬 스토리지에 저장
                localStorage.setItem('userId', response.data.userId); // 여기서 response.data.userId가 null인 경우 확인 필요
                localStorage.setItem('nickname', response.data.nickname);
                this.$router.push('/'); // 로그인 후 메인 페이지로 이동
            })
            .catch(error => {
                console.error('로그인 실패:', error);
                alert('로그인에 실패했습니다.');
            });
        }
    }
  };
  </script>
  
  <style scoped>
  .login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
  }
  
  form {
    display: flex;
    flex-direction: column;
  }
  
  button {
    margin-top: 10px;
  }
  </style>
  