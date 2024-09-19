<template>
    <div class="banner">
      <a href="#" @click="navigator"><img :src="images[currentIndex]" /></a>  
    </div>
</template>

<script>
import main from '@/assets/main.png';

export default {
  data() {
    return {
      currentIndex: 0,
      images: [
        main,
      ]
    };
  },
  methods: {
    navigator(){
      // const userId = localStorage.getItem('userId') ;
      // if(!userId){
      //   alert('로그인이 필요합니다.');
      // } else {
      //   this.$router.push('/select');
      // }
      this.$router.push('/select');
    }
  }
};
</script>

<style scoped>
.banner {
  position: relative;
  width: 100%;
  height: auto;
  margin: 0px;
  padding: 0px;
  overflow: hidden;
}

.banner img {
  width: 100%;
  height: auto;
  display: block;
}
</style>
