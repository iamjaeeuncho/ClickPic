<template>
  <div class="main-container">
    <div class="largeTitle">ENTER THE ROOM</div>
    <div class="container">
      <div class="video-container">
        <div class="photo-origin" ref="photoOrigin" :style="{ backgroundImage: hasCapturedPhoto ? 'url(' + photoImageUrl + ')' : '' }">
          <!-- 프레임 이미지 -->
          <img :src="frameImageUrl" alt="Frame" class="frame-image" />

          <!-- 비디오 (퍼블리셔 + 구독자들) -->
          <div class="video" v-if="!hasCapturedPhoto">
            <user-video
              :stream-manager="publisher"
              @click.native="updateMainVideoStreamManager(publisher)"
              class="video-item"
            />
            <user-video
              v-for="sub in subscribers"
              :key="sub.stream.connection.connectionId"
              :stream-manager="sub"
              @click.native="updateMainVideoStreamManager(sub)"
              class="video-item"
            />
          </div>
        </div>
      </div>
      <div class="right-panel">
        <div>
          <p class="middleTitle">방 이름</p>
          <p class="output">{{ roomInfo.roomName }}({{ userRole }})</p>
          <p class="middleTitle">초대코드</p>
          <p class="output">{{ roomSession }} <button class="btn" @click="copyToClipboard">복사</button></p>
          <p class="middleTitle">안내사항</p>
          <p>1. 입장 순서대로 프레임이 선정됩니다</p>
          <p>2. 인원이 다 차면 자동으로 촬영 버튼이 활성화됩니다</p>
          <p>3. 사진 촬영은 방장만 가능합니다</p>
        </div>
          <button class="btn-rounded" @click="capturePhotoOrigin" :disabled="!isCaptureButtonEnabled || hasCapturedPhoto">
            사진촬영
          </button>
      </div>
    </div>
    <div class="center">
      <button class="btn-large" @click="showLeaveModal">사진 꾸미러가기</button>
    </div>

    <!-- 방 나가기 모달 -->
    <div v-if="isLeaveModalVisible" class="modal">
      <div class="modal-content">
        <p>방을 나가면 다시 못 돌아옵니다</p>
        <p>방을 나가시겠습니까?</p>
        <button @click="leaveSession" class="btn">네</button>
        <button @click="hideLeaveModal" class="btn">아니요</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { OpenVidu } from "openvidu-browser";
import UserVideo from "@/components/video/UserVideo";
import html2canvas from "html2canvas";

axios.defaults.headers.post["Content-Type"] = "application/json";
const APPLICATION_SERVER_URL = "https://4cutstudio.store/";

export default {
  name: "FourFramePage",
  components: {
    UserVideo,
  },
  props: ['roomInfo'],
  data() {
    return {
      OV: undefined,
      session: undefined,
      mainStreamManager: undefined,
      publisher: undefined,
      subscribers: [],
      roomSession: this.$route.params.roomSession,
      userId: this.$route.params.userId,
      userNickname: this.$route.params.userNickname, // 닉네임 추가
      selectedFrame: this.$route.params.frame,
      frameImageUrl: "",
      isLeaveModalVisible: false,
      photoImageUrl: "",
      hasCapturedPhoto: false,
      isHost: this.$route.params.isHost,
      isCaptureButtonEnabled: false,
    };
  },

  computed: {
    userRole() {
      return this.isHost ? '방장' : '참가자';
    },
  },
  methods: {
    copyToClipboard() {
      const textToCopy = this.roomSession;

      const tempInput = document.createElement("textarea");
      tempInput.value = textToCopy;
      document.body.appendChild(tempInput);

      tempInput.select();
      tempInput.setSelectionRange(0, 99999); // 모바일 브라우저 대응
      document.execCommand("copy");

      document.body.removeChild(tempInput);

      alert("복사되었습니다: " + textToCopy);
    },

    setFrameImageUrl() {
      this.frameImageUrl = require(`@/assets/frame/${this.selectedFrame}.png`);
    },
    
    joinSession() {
      this.OV = new OpenVidu();
      this.session = this.OV.initSession();

      // 사진 공유 신호 수신
      this.session.on('signal:photo-shared', event => {
        this.photoImageUrl = event.data;
        this.hasCapturedPhoto = true;
      });

        // 방장이 세션을 떠났을 때 수신하는 신호
  this.session.on('signal:leave-session', () => {
    this.$router.push({
      path: `/edit/${this.roomSession}`,
      query: {
        roomSession: this.roomSession,
        userId: this.userId,
        isHost: this.isHost.toString(),
        photoImageUrl: this.photoImageUrl
      }
    });
  });
      // 새로운 스트림이 생성되었을 때 처리
      this.session.on("streamCreated", ({ stream }) => {
        if (this.subscribers.length >= 3) {
          alert("참가자 수가 최대 한도를 초과했습니다.");
          this.$router.push('/make');
        } else {
          const subscriber = this.session.subscribe(stream);
          this.subscribers.push(subscriber);
          this.updateCaptureButtonState(); 
          this.$nextTick(this.updateVideoStyles);
        }
      });

      // OpenVidu 토큰을 받아 세션에 연결
      this.getToken(this.roomSession).then((token) => {
        this.session
          .connect(token, { clientData: JSON.stringify({ userId: this.userId, nickname: this.userNickname }) }) // 닉네임과 userId 전달
          .then(() => {
            let publisher = this.OV.initPublisher(undefined, {
              audioSource: undefined, 
              videoSource: undefined, 
              publishAudio: true, 
              publishVideo: true, 
              frameRate: 30,
              insertMode: "APPEND",
              mirror: false,
              resolution: "600x800"
            });
            this.mainStreamManager = publisher;
            this.publisher = publisher;
            this.session.publish(this.publisher);
            this.$nextTick(this.updateVideoStyles);
          })
          .catch((error) => {
            console.log("Error connecting to session:", error.code, error.message);
          });
      });

      this.session.on("streamDestroyed", ({ stream }) => {
        const index = this.subscribers.indexOf(stream.streamManager, 0);
        if (index >= 0) {
          this.subscribers.splice(index, 1);
          this.updateCaptureButtonState(); 
        }
      });

      this.session.on("exception", ({ exception }) => {
        console.warn(exception);
      });

      window.addEventListener("beforeunload", this.leaveSession);
    },
    leaveSession() {
  if (this.session) {
    // 방장이 모든 사용자에게 신호 전송
    this.session.signal({
      to: [], // 모든 사용자에게 전송
      type: 'leave-session' // 신호 타입
    }).then(() => {
      // 세션에서 방장 자신도 떠남
      this.session = undefined;
      this.mainStreamManager = undefined;
      this.publisher = undefined;
      this.subscribers = [];
      this.OV = undefined;
      window.removeEventListener("beforeunload", this.leaveSession);

      // 방장은 바로 edit 페이지로 이동
      this.$router.replace({
        path: `/edit/${this.roomSession}`,
        query: {
          roomSession: this.roomSession,
          userId: this.userId,
          isHost: this.isHost.toString(),
          canvasWidth: 310,
          canvasHeight: 800,
          photoImageUrl: this.photoImageUrl,
        },
      }).catch(err => {
        if (err.name !== 'NavigationDuplicated') {
          console.error(err);
        }
      });
    }).catch(error => {
      console.error("Error sending signal: ", error);
    });
  }
},


    showLeaveModal() {
      this.isLeaveModalVisible = true;
    },

    hideLeaveModal() {
      this.isLeaveModalVisible = false;
    },

    updateMainVideoStreamManager(stream) {
      if (this.mainStreamManager === stream) return;
      this.mainStreamManager = stream;
    },

    async getToken(roomSession) {
      const roomSessionFromServer = await this.createSession(roomSession);
      return await this.createToken(roomSessionFromServer);
    },

    async createSession(roomSessionFromServer) {
      const response = await axios.post(
        APPLICATION_SERVER_URL + "api/sessions",
        { customSessionId: roomSessionFromServer },
        {
          headers: {
            "Content-Type": "application/json",
          }
        }
      );
      return response.data;
    },

    async createToken(roomSessionFromServer) {
      const response = await axios.post(
        APPLICATION_SERVER_URL + "api/sessions/" + roomSessionFromServer + "/connections",
        {},
        {
          headers: {
            "Content-Type": "application/json",
          }
        }
      );
      return response.data;
    },
    
    updateVideoStyles() {
      const videos = this.$el.querySelectorAll("video");
      videos.forEach((video) => {
        video.style.width = "100%";
        video.style.height = "100%";
        video.style.objectFit = "cover";
      });
    },

    updateCaptureButtonState() {
      this.isCaptureButtonEnabled = this.subscribers.length >= 3;
    },

    async capturePhotoOrigin() {
      if (!this.isHost) {
        alert("방장만 사진을 촬영할 수 있습니다");
        return;
      }

      const element = this.$refs.photoOrigin;

      try {
        const canvas = await html2canvas(element, {
          width: 600,   
          height: 800,  
        });
        const imageData = canvas.toDataURL("image/png");

        this.photoImageUrl = imageData;
        this.hasCapturedPhoto = true;
        this.isCaptureButtonEnabled = false;

        const response = await axios.post(
          `${process.env.VUE_APP_BACKEND_URL}/photo/save`,
          { 
            originPhoto: imageData,
            roomId: this.roomInfo.roomId
          },
          {
            headers: {
              "Content-Type": "application/json",
            }
          }
        );

        this.photoImageUrl = response.data;  
        console.log("찍혀라;",this.photoImageUrl)
        this.session.signal({
          data: this.photoImageUrl, 
          to: [],                   
          type: 'photo-shared'       
        });

        alert('이미지가 성공적으로 촬영되었습니다');
      } catch (error) {
        console.error("Error capturing or saving image:", error.response ? error.response.data : error.message);
      }
    },
  },
  updated() {
    this.updateVideoStyles();
  },
  created() {
    this.setFrameImageUrl();
    this.joinSession();
  }
};
</script>

<style scoped>
.main-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  flex-grow: 1;
  margin-top: 20px;
}

.video-container {
  margin-right: 180px;
}

.controls-container {
  width: 300px; 
  display: flex;
  flex-direction: column;
  align-items: center;
}

.photo-origin {
  position: relative;
  width: 600px;
  height: 800px;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
}

.photo-origin img {
  width: 100%;
  height: auto;
}

.frame-image {
  position: absolute;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.video {
  display: grid;
  position: absolute;
  width: 100%;
  height: 100%;
  grid-template-columns: 300px 300px;
  grid-template-rows: 350px 350px;
  grid-gap: 0;
  z-index: 1;
}

.video-item {
  width: 100%;
  height: 100%;
  object-fit: cover;
  margin: 0;
  padding: 0;
  transform: scaleX(-1);
}

.right-panel {
    width: 100%;
    max-width: 300px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 15px;
  text-align: center;
}

.btn {
  margin-right: 10px;
}

.btn-rounded:disabled {
  background-color: #c0c0c0;
  border: none;
  color: #6c757d;
  cursor: not-allowed;
}

</style>