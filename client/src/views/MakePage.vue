<template>
  <div>
    <div class="largeTitle">MAKE A ROOM</div>
    <!-- <div class="center">
      <p class="middleTitle">참여자</p>
      <input v-model="userId" class="form-control" type="text" required />
    </div> -->
    <div class="center">
      <p class="middleTitle">방 이름 입력하기</p>
      <input v-model="roomName" class="form-control" type="text" required />
    </div>
    <div class="center">
      <p class="middleTitle">방 인원 선택하기</p>
      <button @click="setUserCount(1)" :class="{'btn-selected': userCount === 1, 'btn-empty': userCount !== 1}">1명</button>
      <button @click="setUserCount(2)" :class="{'btn-selected': userCount === 2, 'btn-empty': userCount !== 2}">2명</button>
      <button @click="setUserCount(4)" :class="{'btn-selected': userCount === 4, 'btn-empty': userCount !== 4}">4명</button>
    </div>
    <div class="center">
      <p class="middleTitle">프레임 선택하기</p>
      <p class="frameText">기본</p>
      <div class="frame-options">
        <div v-for="(frame, index) in currentFrames" :key="index" class="frame-option">
          <img 
            :src="frame.src" 
            :alt="frame.name" 
            class="frame-image" 
            :class="{'selected-frame': selectedFrame === frame.name}" 
            @click="selectFrame(frame)" 
          />
        </div>
      </div>
      <p class="frameText">패턴</p>
      <div class="frame-options">
        <div v-for="(frame, index) in currentPatternFrames" :key="index" class="frame-option">
          <img 
            :src="frame.src" 
            :alt="frame.name" 
            class="frame-image" 
            :class="{'selected-frame': selectedFrame === frame.name}" 
            @click="selectFrame(frame)" 
          />
        </div>
      </div>
    </div>
    <div class="center marginBottom">
      <button class="btn-large" @click="joinSession">방 만들기</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';

export default {
  name: "MakePage",
  data() {
    return {
      userId: localStorage.getItem("userId"),
      // userId: Math.floor(Math.random() * 100),
      roomName: "",
      roomSession: "",
      userCount: 1,
      frames: {
        1: [
          { name: '1frame_black', src: require('@/assets/frame/1frame_black.png') },
          { name: '1frame_blue', src: require('@/assets/frame/1frame_blue.png') },
          { name: '1frame_green', src: require('@/assets/frame/1frame_green.png') },
          { name: '1frame_purple', src: require('@/assets/frame/1frame_purple.png') },
          { name: '1frame_red', src: require('@/assets/frame/1frame_red.png') },
          { name: '1frame_white', src: require('@/assets/frame/1frame_white.png') },
        ],
        2: [
          { name: '2frame_black', src: require('@/assets/frame/2frame_black.png') },
          { name: '2frame_blue', src: require('@/assets/frame/2frame_blue.png') },
          { name: '2frame_green', src: require('@/assets/frame/2frame_green.png') },
          { name: '2frame_purple', src: require('@/assets/frame/2frame_purple.png') },
          { name: '2frame_red', src: require('@/assets/frame/2frame_red.png') },
          { name: '2frame_white', src: require('@/assets/frame/2frame_white.png') },
        ],
        4: [
          { name: '4frame_black', src: require('@/assets/frame/4frame_black.png') },
          { name: '4frame_blue', src: require('@/assets/frame/4frame_blue.png') },
          { name: '4frame_green', src: require('@/assets/frame/4frame_green.png') },
          { name: '4frame_purple', src: require('@/assets/frame/4frame_purple.png') },
          { name: '4frame_red', src: require('@/assets/frame/4frame_red.png') },
          { name: '4frame_white', src: require('@/assets/frame/4frame_white.png') },
        ]
      },
      frames_pattern: {
        1: [
          { name: '1frame_black_pattern', src: require('@/assets/frame/1frame_black_pattern.png') },
          { name: '1frame_blue_pattern', src: require('@/assets/frame/1frame_blue_pattern.png') },
          { name: '1frame_green_pattern', src: require('@/assets/frame/1frame_green_pattern.png') },
          { name: '1frame_purple_pattern', src: require('@/assets/frame/1frame_purple_pattern.png') },
          { name: '1frame_red_pattern', src: require('@/assets/frame/1frame_red_pattern.png') },
          { name: '1frame_white_pattern', src: require('@/assets/frame/1frame_white_pattern.png') },
        ],
        2: [
          { name: '2frame_black_pattern', src: require('@/assets/frame/2frame_black_pattern.png') },
          { name: '2frame_blue_pattern', src: require('@/assets/frame/2frame_blue_pattern.png') },
          { name: '2frame_green_pattern', src: require('@/assets/frame/2frame_green_pattern.png') },
          { name: '2frame_purple_pattern', src: require('@/assets/frame/2frame_purple_pattern.png') },
          { name: '2frame_red_pattern', src: require('@/assets/frame/2frame_red_pattern.png') },
          { name: '2frame_white_pattern', src: require('@/assets/frame/2frame_white_pattern.png') },
        ],
        4: [
          { name: '4frame_black_pattern', src: require('@/assets/frame/4frame_black_pattern.png') },
          { name: '4frame_blue_pattern', src: require('@/assets/frame/4frame_blue_pattern.png') },
          { name: '4frame_green_pattern', src: require('@/assets/frame/4frame_green_pattern.png') },
          { name: '4frame_purple_pattern', src: require('@/assets/frame/4frame_purple_pattern.png') },
          { name: '4frame_red_pattern', src: require('@/assets/frame/4frame_red_pattern.png') },
          { name: '4frame_white_pattern', src: require('@/assets/frame/4frame_white_pattern.png') },
        ]
      },
      selectedFrame: null,
      isHost: true
    };
  },
  computed: {
    currentFrames() {
      return this.frames[this.userCount] || [];
    },
    currentPatternFrames() {
      return this.frames_pattern[this.userCount] || [];
    }
  },
  mounted() {
  console.log("userId:", this.userId);
  },
  methods: {
    setUserCount(count) {
      this.userCount = count; 
    },
    selectFrame(frame) {
      this.selectedFrame = frame.name;
    },
    async joinSession() {
      if (this.roomName) {
        this.roomSession = uuidv4();

        const payload = {
          userId: Number(this.userId),
          userCount: this.userCount,
          roomName: this.roomName,
          roomSession: this.roomSession,
          frameId: this.selectedFrame
        };
        
        try {
          await axios.post(`${process.env.VUE_APP_BACKEND_URL}/room/create`, payload);

          this.$router.push({
            name: 'Room',
            params: {
              roomSession: this.roomSession,
              userId: this.userId,
              userCount: this.userCount,
              frame: this.selectedFrame,
              isHost: true
            }
          });

        } catch (error) {
          alert('Error creating room: ' + error);
        }
      } else {
        alert("방 이름을 입력하세요");
      }
    }
  }
};
</script>

<style scoped>
.frame-options {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.frame-option {
  text-align: center;
}

.frame-image {
  width: 150px;
  height: auto;
  cursor: pointer;
  transition: transform 0.2s;
}

.frame-image:hover {
  transform: scale(1.1);
}

.selected-frame {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s;
}
.frameText{
  font-size: bold;
  color:#675C5C;
}
.btn-large{
  font-size: 20px;
}
</style>
