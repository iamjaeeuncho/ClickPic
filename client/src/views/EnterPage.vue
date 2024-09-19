<template>
  <div>
    <div class="largeTitle">ENTER THE ROOM</div>
    <!-- <div class="center">
      <p class="middleTitle">참여자</p>
      <input v-model="userId" class="form-control" type="text" required />
    </div> -->
    <div class="center">
      <p class="middleTitle">방 코드 입력하기</p>
      <input v-model="roomSession" class="form-control" type="text" required />
    </div>
    <div class="center marginBottom">
      <button class="btn" @click="joinSession">방 입장하기</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "EnterPage",
  data() {
    return {
      userId: localStorage.getItem("userId"),
      // userId: Math.floor(Math.random() * 100),
      roomSession: "",
    };
  },
  mounted() {
  console.log("userId:", this.userId);
  },
  methods: {
    async joinSession() {
      if (!this.roomSession) {
        alert("방 코드를 입력하세요");
        return;
      }

      try {
        const response = await axios.post(`${process.env.VUE_APP_BACKEND_URL}/room/enter`, null, {
          params: {
            userId: this.userId,
            roomSession: this.roomSession
          }
        });

        if (response.status === 200) {
          const roomInfo = await this.fetchRoomInfo(this.roomSession);

          if (roomInfo) {
            this.$router.push({
              name: 'Room',
              params: {
                roomSession: this.roomSession,
                userId: this.userId,
                userCount: roomInfo.userCount,
                frame: roomInfo.frameId,
                isHost: false
              }
            });
          } else {
            alert('방 정보를 가져오는 데 실패했습니다. 다시 시도하세요.');
          }
        } else {
          alert('유효하지 않은 세션 코드입니다. 다시 시도하세요.');
        }
      } catch (error) {
        alert('방 입장에 실패했습니다. 다시 시도하세요.');
      }
    },
    async fetchRoomInfo(roomSession) {
      try {
        const response = await axios.get(`${process.env.VUE_APP_BACKEND_URL}/room/info`, {
          params: {
            roomSession: roomSession
          }
        });

        if (response.status === 200) {
          return response.data;
        } else {
          console.error("Failed to fetch room info:", response.data);
          return null;
        }
      } catch (error) {
        console.error("Error fetching room info:", error);
        return null;
      }
    }
  }
};
</script>

<style scoped>
</style>