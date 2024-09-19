<template>
  <div class="mypage-container">
    <h2>My Page</h2>
    <div v-if="paginatedPhotos.length > 0" class="photo-list">
      <div v-for="photo in paginatedPhotos" :key="photo.PHOTO_ID" class="photo-item" @click="goToDetail(photo.PHOTO_ID)">
        <div class="left-content">
          <img :src="photo.COMPLETE_PHOTO" alt="photo" class="photo-image"> 
          <div class="photo-info">
            <p class="room-name">{{ photo.ROOM_NAME }}</p>
            <p class="note">{{ photo.NOTE }}</p>
          </div>
        </div>
        <div class="right-content">
          <p class="user-count">총 {{ photo.USER_COUNT }}명</p>
          <p class="create-date">{{ formatDate(photo.CREATED_AT) }}</p>
        </div>
      </div>
    </div>
    <div v-else>
      <p>사진이 없습니다.</p>
    </div>

    <!-- 페이지네이션 -->
    <div class="pagination">
      <span @click="changePage(currentPage - 1)" :class="{ disabled: currentPage === 1 }" class="arrow">‹</span>
      <button
        v-for="page in paginationRange"
        :key="page"
        @click="changePage(page)"
        :disabled="page === currentPage"
      >
        {{ page }}
      </button>
      <span @click="changePage(currentPage + 1)" :class="{ disabled: currentPage === totalPages }" class="arrow">›</span>
    </div>

    <button @click="showConfirmModal = true" class="delete-button">회원 탈퇴</button>

    <!-- 확인 모달 -->
    <ModalComponent
      v-if="showConfirmModal"
      messageType="confirm"
      message="정말 탈퇴하시겠습니까?" 
      @close="showConfirmModal = false"
      :onConfirm="confirmDelete"
    />
    <!-- 완료 모달 -->
    <ModalComponent
      v-if="showCompletionModal"
      messageType="completion"
      message="회원 탈퇴가 완료되었습니다."
      @close="showCompletionModal = false"
    />
  </div>
</template>

<script>
import ModalComponent from '@/components/ModalComponent.vue'; // 모달 컴포넌트 임포트
import axios from 'axios';

export default {
  components: {
    ModalComponent, // 모달 컴포넌트 등록
  },
  data() {
    return {
      showConfirmModal: false,
      showCompletionModal: false,
      photos: [], // 사진 목록을 저장할 배열
      currentPage: 1, // 현재 페이지
      photosPerPage: 5, // 페이지당 사진 수
    };
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo;
    },
    userId() {
      return this.$store.state.userId; // userId를 사용해 로그인 여부 확인
    },
    totalPages() {
      return Math.ceil(this.photos.length / this.photosPerPage);
    },
    paginatedPhotos() {
      const start = (this.currentPage - 1) * this.photosPerPage;
      const end = start + this.photosPerPage;
      return this.photos.slice(start, end); // 현재 페이지에 맞는 사진들을 반환
    },
    paginationRange() {
      const rangeSize = 5;
      let start = Math.max(this.currentPage - Math.floor(rangeSize / 2), 1);
      let end = Math.min(start + rangeSize - 1, this.totalPages);

      if (end - start < rangeSize - 1) {
        start = Math.max(end - rangeSize + 1, 1);
      }

      return Array.from({ length: end - start + 1 }, (_, i) => start + i);
    }
  },
  methods: {
    fetchUserPhotos() {
      const userId = localStorage.getItem('userId'); // 로컬 스토리지에서 userId 가져오기

      if (!userId) {
        console.error('UserId가 없습니다. 로그인이 필요합니다.');
        this.$router.push('/login');
        return;
      }

      axios.get(`${process.env.VUE_APP_BACKEND_URL}/api/photolist`, {
        params: {
          userId: userId // userId를 쿼리 파라미터로 추가
        },
        withCredentials: true // 쿠키를 포함한 요청을 위해 withCredentials 설정
      })
      .then(response => {
        this.photos = response.data; // 서버에서 받은 사진 데이터를 저장
      })
      .catch(error => {
        console.error('사진 목록 불러오기 실패:', error);
      });
    },
    formatDate(dateString) {
      if (!dateString) {
        return 'N/A';
      }

      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return 'N/A';
      }

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');

      return `${year}-${month}-${day}`; // YYYY-MM-DD 형식으로 반환
    },
    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    goToDetail(photoId) {
      this.$router.push({ name: 'PictureDetail', params: { photoId } }); // 사진의 상세 페이지로 이동
    },
    confirmDelete() {
      const userId = localStorage.getItem('userId'); // 로컬 스토리지에서 userId 가져오기

      if (!userId) {
          console.error('UserId가 없습니다. 로그인이 필요합니다.');
          this.$router.push('/login');
          return;
      }

      axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/deactivate`, { userId: userId }, {
          withCredentials: true // 세션 대신 로컬 스토리지 기반 인증을 사용
      })
      .then(() => {
          this.showConfirmModal = false;
          this.showCompletionModal = true;
          this.$store.commit('logout');

          setTimeout(() => {
              this.$router.push('/');
          }, 2000);
      })
      .catch(error => {
          console.error('회원 탈퇴 실패:', error);
          alert("회원 탈퇴 중 오류가 발생했습니다.");
      });
  }
  },
  mounted() {
    const userId = localStorage.getItem('userId'); // 로그인 여부 확인

    if (!userId) {
      alert("로그인이 필요합니다.");
      this.$router.push('/login');
    } else {
      this.fetchUserPhotos(); // 마운트 시 사용자 사진 데이터를 불러옴
    }
  }
}
</script>


<style scoped>
/* 기존 스타일 그대로 유지 */
.mypage-container {
  padding: 20px;
  max-width: 800px;
  margin: auto;
}

h2 {
  color: #DB574D;
  text-align: center;
  font-size: 35px;
  margin-bottom: 50px;
}

.photo-list {
  margin-top: 20px;
}

.photo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ddd;
  cursor: pointer; /* 클릭 가능하도록 커서를 추가 */
}

.left-content {
  display: flex;
  align-items: center;
}

.photo-image {
  width: 100px;
  height: 125px;
  margin-right: 15px;
  object-fit: cover;
}

.photo-info {
  display: flex;
  flex-direction: column;
}

.room-name {
  font-weight: bold;
  font-size: 20px;
}

.note {
  font-size: 14px;
  color: #666;
  margin-left: 5px;
  margin-top: 5px;
}

.right-content {
  text-align: right;
}

.user-count {
  font-size: 18px;
  margin-bottom: 5px;
  color: #333;
}

.create-date {
  font-size: 24px;
  color: #DB574D;
}

.delete-button {
  display: block;
  width: 150px;
  margin: 30px auto;
  padding: 10px 20px;
  background-color: #DB574D;
  color: white;
  border: none;
  cursor: pointer;
  margin-top: 100px;
}

/* 페이지네이션 스타일 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.pagination button {
  background-color: #999;
  color: white;
  border: none;
  padding: 10px 20px;
  margin: 0 5px;
  cursor: pointer;
  font-size: 18px;
}

.pagination button:disabled {
  background-color: #DB574D;
  cursor: default;
}

.arrow {
  font-size: 24px; /* 화살표 크기를 키움 */
  margin: 0 15px; /* 버튼과의 간격 설정 */
  color: #999; /* 기본 상태 화살표 색상 */
  cursor: pointer;
}

.arrow.disabled {
  color: #ccc; /* 비활성화된 상태에서는 회색으로 표시 */
  cursor: default;
}
</style>
