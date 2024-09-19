<template>
  <div class="container">
    <div class="select">
      <router-link class="link" to="/make">MAKE A ROOM</router-link>
    </div>
    <div class="select">
      <router-link class="link" to="/enter">ENTER THE ROOM</router-link>
    </div>
  </div>
</template>

<script>
export default {

}
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 80vh;
}

.select {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 150px;
  font-size: 60px;
  color: black;
  text-align: center;
}

.select:hover {
  background: #D8D8D8;
  color: #DB574D;
}

.link {
  text-decoration: none;
  color: inherit;
}

.link:hover {
  color: inherit;
}
</style>
