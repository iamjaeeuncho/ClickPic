<template>
  <div class="community-container">
    <h2>COMMUNITY</h2>

    <!-- 정렬 옵션 -->
    <p class="sort-options">
      <span @click="sortByDate" :class="{ active: sortType === 'date' }">최신순</span> /
      <span @click="sortByPopularity" :class="{ active: sortType === 'popularity' }">인기순</span>
    </p>

    <!-- 사진 그리드 -->
    <div class="photo-grid">
      <div v-for="photo in sortedPhotos" :key="photo.PHOTO_ID" class="photo-item">
        <div class="photo-wrapper">
          <img
            :src="photo.COMPLETE_PHOTO"
            alt="Community Photo"
            class="photo-image"
            @click="goToDetail(photo.PHOTO_ID)"
          />
          <!-- 좋아요 표시 및 좋아요 토글 -->
          <div class="like-count-overlay">
            <button @click.stop="toggleLike(photo)">
              <img 
                :src="isLiked(photo.PHOTO_ID) ? filledHeart : emptyHeart" 
                alt="Like Heart" 
                class="like-heart-image"
              />
            </button>
            <span class="like-count">{{ photo.likesCount }}</span>
          </div>
        </div>
        <p>{{ photo.room_name }}</p>
      </div>
    </div>

    <!-- 페이징 -->
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
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import axios from "axios";
import emptyHeart from '@/assets/red_empty_heart.png';
import filledHeart from '@/assets/filled_heart.png';

export default {
  data() {
    return {
      photos: [], // 전체 커뮤니티 사진
      sortType: "date", // 기본 정렬 방식 (최신순)
      currentPage: 1, // 현재 페이지
      photosPerPage: 12, // 페이지당 사진 개수
      totalPhotos: 0, // 총 사진 개수
      filledHeart, // 채워진 하트 이미지
      emptyHeart, // 빈 하트 이미지
    };
  },
  watch: {
    // sortType이 변경될 때마다 다시 fetchCommunityPhotos 호출
    sortType() {
      this.fetchCommunityPhotos();
    },
  },
  computed: {
    ...mapGetters(["isLiked"]),

    // 총 페이지 수 계산
    totalPages() {
      return Math.ceil(this.totalPhotos / this.photosPerPage);
    },

    // 정렬된 사진 배열 계산 (최신순/인기순 정렬 및 페이징 처리)
    sortedPhotos() {
      let sorted = [...this.photos]; // 배열 복사

      // 인기순 정렬 (likesCount 기준)
      if (this.sortType === "popularity") {
        sorted.sort((a, b) => b.likesCount - a.likesCount);
      }
      // 최신순 정렬 (created_at 기준)
      else {
        sorted.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
      }

      // 페이징 처리
      return sorted.slice(
        (this.currentPage - 1) * this.photosPerPage,
        this.currentPage * this.photosPerPage
      );
    },

    // 페이징 버튼 범위 계산
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
    ...mapActions(["toggleLike", "fetchLikedPhotos"]),

    // 커뮤니티 사진 데이터 가져오기
    fetchCommunityPhotos() {
      axios
        .get(`${process.env.VUE_APP_BACKEND_URL}/api/community`, {
          params: { sortType: this.sortType }, // sortType 전달
        })
        .then((response) => {
          this.photos = response.data.map((photo) => ({
            ...photo,
            likesCount: photo.likesCount || 0,
            PHOTO_ID: photo.PHOTO_ID || photo.id || photo.photoId,
          }));
          this.totalPhotos = this.photos.length;

          // 각 사진에 대한 likesCount 가져오기
          this.photos.forEach((photo) => {
            this.fetchLikesCount(photo.PHOTO_ID);
          });
        })
        .catch((error) => {
          console.error("커뮤니티 사진 불러오기 실패:", error);
        });
    },

    // API 호출로 좋아요 수 가져오기
    fetchLikesCount(photoId) {
      axios
        .get(`${process.env.VUE_APP_BACKEND_URL}/api/likescount/${photoId}/likesCount`)
        .then((response) => {
          const photo = this.photos.find((p) => p.PHOTO_ID === photoId);
          if (photo) {
            photo.likesCount = response.data;
          }
        })
        .catch((error) => {
          console.error(`Failed to fetch likes count for photoId: ${photoId}`, error);
        });
    },

    // 좋아요 토글
    toggleLike(photo) {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        alert('로그인이 필요합니다.');
        return;
      }

      axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/like/${photo.PHOTO_ID}?userId=${userId}`)
        .then(() => {
          if (this.isLiked(photo.PHOTO_ID)) {
            photo.likesCount -= 1;
          } else {
            photo.likesCount += 1;
          }
          this.$store.dispatch('toggleLike', photo.PHOTO_ID);
        })
        .catch(error => {
          console.error('좋아요 상태 변경 실패:', error);
        });
    },

    // 최신순 정렬
    sortByDate() {
      this.sortType = "date";
    },

    // 인기순 정렬
    sortByPopularity() {
      this.sortType = "popularity";
    },

    // 페이지 변경
    changePage(page) {
      if (page > 0 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },

    // 사진 상세 페이지로 이동
    goToDetail(photoId) {
      this.$router.push({ name: "PictureDetail", params: { photoId } });
    },
  },

  // 컴포넌트 마운트 시 사진 데이터를 불러옴
  mounted() {
    this.fetchCommunityPhotos(); // 로그인 여부와 상관없이 커뮤니티 사진을 불러옴
    this.$store.dispatch("fetchLikedPhotos"); // 좋아요 상태는 로그인한 유저에만 적용
  },
};
</script>


<style scoped>
.community-container {
  padding: 20px;
  max-width: 1200px;
  margin: auto;
}

h2 {
  color: #db574d;
  text-align: center;
  font-size: 35px;
  margin-bottom: 50px;
}

.sort-options {
  text-align: left;
  font-size: 18px;
  color: #666;
  margin-bottom: 50px;
}

.sort-options span {
  cursor: pointer;
}

.sort-options .active {
  color: #db574d;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.photo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.photo-wrapper {
  position: relative;
}

.photo-image {
  width: 100%;
  height: 380px;
  cursor: pointer;
  object-fit: contain;
}

.photo-item p {
  margin-top: 10px;
  font-size: 16px;
  color: #333;
}
  
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
  background-color: #db574d;
  cursor: default;
}

.arrow {
  font-size: 24px;
  margin: 0 15px;
  color: #999;
  cursor: pointer;
}

.arrow.disabled {
  color: #ccc;
  cursor: default;
}
/* 좋아요 스타일 추가 */
.like-count-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 16px;
  color: #fff;
}

.like-icon {
margin-right: 3px;
}

button {
background: none;
border: none;
cursor: pointer;
font-size: 16px;
color: #fff;
}

button.liked {
color: red;
font-size: 20px;
-webkit-text-stroke: 0.5px #fff;
}

.like-count {
margin-left: 5px;
font-size: 16px;
}

.like-heart-image {
width: 18px; /* 하트 아이콘 크기 */
height: 16px;
cursor: pointer;
}
</style>
