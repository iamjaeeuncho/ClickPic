<template>
    <div class="register-container">
      <h1>회원가입</h1>
      <form @submit.prevent="registerUser">
        <div>
          <label for="userId">아이디:</label>
          <input type="text" id="userId" v-model="userId" required />
        </div>
        <div>
          <label for="nickname">닉네임:</label>
          <input type="text" id="nickname" v-model="nickname" required />
        </div>
        <div>
          <label for="password">비밀번호:</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <div>
          <label for="confirmPassword">비밀번호 확인:</label>
          <input type="password" id="confirmPassword" v-model="confirmPassword" required />
        </div>
        <button type="submit" class="btn-large">회원가입</button>
      </form>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        userId: '',  // 아이디 필드 추가
        nickname: '',
        password: '',
        confirmPassword: ''
      };
    },
    methods: {
      registerUser() {
        if (this.password !== this.confirmPassword) {
          alert('비밀번호가 일치하지 않습니다.');
          return;
        }
  
        // userId, nickname, password를 백엔드로 전송
        axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/register`, {
          userId: this.userId,  // userId를 백엔드로 전송
          nickname: this.nickname,  // nickname 전송
          password: this.password  // password 전송
        })
        .then(() => {
          alert('회원가입이 성공적으로 완료되었습니다.');
          this.$router.push('/login');
        })
        .catch(error => {
          console.error('회원가입 중 오류 발생:', error);
          alert('회원가입에 실패했습니다.');
        });
      }
    }
  };
  </script>
  
  <style scoped>
  .register-container {
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
  
  

  