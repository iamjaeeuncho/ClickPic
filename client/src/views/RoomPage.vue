<template>
  <div>
    <component v-if="roomInfo && currentComponent" :is="currentComponent" :room-info="roomInfo" />
  </div>
</template>

<script>
import axios from 'axios';
import OneFramePage from '@/components/room/OneFramePage.vue';
import TwoFramePage from '@/components/room/TwoFramePage.vue';
import ThreeFramePage from '@/components/room/ThreeFramePage.vue';
import FourFramePage from '@/components/room/FourFramePage.vue';

export default {
  name: 'RoomPage',
  props: ['roomSession', 'userCount', 'isHost'],
  components: {
    OneFramePage,
    TwoFramePage,
    ThreeFramePage,
    FourFramePage,
  },
  data() {
    return {
      roomInfo: {},
    };
  },
  created() {
    this.loadRoomInfo();
  },
  computed: {
    currentComponent() {
      switch (this.userCount) {
        case 1:
          return 'OneFramePage';
        case 2:
          return 'TwoFramePage';
        case 3:
          return 'ThreeFramePage';
        case 4:
          return 'FourFramePage';
        default:
          return null;
      }
    },
  },
  methods: {
    async loadRoomInfo() {
      if (Object.keys(this.roomInfo).length === 0) { // 빈 객체일 때만 로드
        try {
          const response = await axios.get(`${process.env.VUE_APP_BACKEND_URL}/room/info`, {
            params: {
              roomSession: this.roomSession,
            },
          });

          if (response.status === 200) {
            this.roomInfo = response.data;
          } else {
            console.error("Failed to load room info:", response.data);
          }
        } catch (error) {
          console.error("Error loading room info:", error);
        }
      }
    }
  },
};
</script>
