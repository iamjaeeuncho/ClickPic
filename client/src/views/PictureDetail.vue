<template>
  <div class="main-container">
    <div class="largeTitle">PICTURE</div>
    <div class="container">
      <div class="left-side">
        <img :src="photoDetail.COMPLETE_PHOTO" alt="Detail Photo" class="photo-image" />
      </div>
      <div class="right-side">
          <p class="smallTitle">{{ formatDate(photoDetail.CREATED_AT) }}</p>
          <div class="title-like-container">
              <p class="middleTitle">{{ photoDetail.ROOM_NAME }}</p>
              <div class="like-section">
              <button @click="toggleLike">
                  <img 
                      :src="isLiked ? filledHeart : emptyHeart" 
                      alt="Like Heart" 
                      class="like-heart-image"
                  />
              </button>
              <span class="like-count">{{ likesCount }}</span>
              </div>
          </div>

        <p>{{ photoDetail.NOTE || '메모가 없습니다.' }}</p>

      <!-- 구분선 추가 -->
      <hr class="divider" />
      

        <!-- 댓글 섹션 -->
        <div class="comment-section">
          <div class="comment-header">
            <p class="middleTitle">댓글</p>
            <!-- 더보기/접기 버튼 (드롭다운 형식) -->
            <button @click="toggleComments" class="dropdown-toggle">
              <span v-if="showAllComments">▲</span>
              <span v-else>▼</span>
            </button>
          </div>
          <ul class="comment-list">
            <li v-for="comment in visibleComments" :key="comment.commentId" class="comment-item">
              <div class="comment-content">
                <p class="nickName"><strong>{{ comment.nickname }}</strong></p>
                <p>{{ comment.content }}</p>
              </div>
              <p class="comment-date">{{ formatDate(comment.createdAt) }}</p>
            </li>
          </ul>

          <textarea v-model="newComment" placeholder="댓글을 입력하세요."></textarea>
          <div class="right">
            <button @click="addComment">등록</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import axios from 'axios';
import emptyHeart from '@/assets/empty_heart.png';
import filledHeart from '@/assets/filled_heart.png';

export default {
  data() {
    return {
      photoDetail: {}, // 사진 상세 정보
      isLiked: false, // 좋아요 상태
      likesCount: 0, // 좋아요 개수
      comments: [], // 댓글 목록
      newComment: '', // 새 댓글
      showAllComments: true, // 기본값을 true로 설정하여 드롭다운이 처음에 펴진 상태로 시작
      filledHeart,
      emptyHeart,
    };
  },
  computed: {
      visibleComments() {
          return this.showAllComments ? this.comments : [];
      },
  },
  methods: {
    // 사진 상세 정보 및 좋아요 상태 불러오기
    fetchPhotoDetail(photoId) {
      axios
        .get(`${process.env.VUE_APP_BACKEND_URL}/api/community/${photoId}`)
        .then((response) => {
          this.photoDetail = response.data;

          // 좋아요 수 불러오기
          axios
            .get(`${process.env.VUE_APP_BACKEND_URL}/api/like/${photoId}`)
            .then((res) => {
              this.likesCount = res.data;
            })
            .catch((error) => {
              console.error('좋아요 수를 불러오지 못했습니다.', error);
            });

          const userId = localStorage.getItem('userId');
          if (userId) {
            axios
              .get(`${process.env.VUE_APP_BACKEND_URL}/api/like/${photoId}/is-liked`, {
                params: { userId },
              })
              .then((res) => {
                this.isLiked = res.data;
              })
              .catch((error) => {
                console.error('좋아요 여부 확인 중 오류가 발생했습니다.', error);
              });
          }

          // 댓글 불러오기
          this.fetchComments(photoId);
        })
        .catch((error) => {
          console.error('사진 정보를 불러오지 못했습니다.', error);
        });
    },

    // 댓글 불러오기
    fetchComments(photoId) {
      axios
        .get(`${process.env.VUE_APP_BACKEND_URL}/api/comments/${photoId}`)
        .then((response) => {
          console.log('Fetched comments data:', response.data);

          // 댓글을 createdAt 필드를 기준으로 정렬 (오래된 순)
          this.comments = response.data.sort((a, b) => {
            return new Date(a.createdAt) - new Date(b.createdAt);
          });
        })
        .catch((error) => {
          console.error('댓글을 불러오는 중 오류가 발생했습니다.', error);
        });
    },

    // 댓글 등록
    addComment() {
      if (!this.newComment.trim()) {
        alert('댓글을 입력하세요.');
        return;
      }

      const userId = localStorage.getItem('userId');
      if (!userId) {
        alert('로그인이 필요합니다.');
        return;
      }

      const comment = {
        userId: userId,
        photoId: this.$route.params.photoId,
        content: this.newComment,
        createdAt: new Date().toISOString(),
      };

      axios
        .post(`${process.env.VUE_APP_BACKEND_URL}/api/comments`, comment)
        .then(() => {
          this.fetchComments(this.$route.params.photoId); // 새로 댓글 목록 가져오기
          this.newComment = ''; // 입력 필드 초기화
        })
        .catch((error) => {
          console.error('댓글 등록에 실패했습니다.', error);
        });
    },

    // 더보기/접기 버튼 동작 (드롭다운 형식)
    toggleComments() {
      this.showAllComments = !this.showAllComments;
    },

    // 좋아요 토글
    toggleLike() {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        alert('로그인이 필요합니다.');
        return;
      }

      axios
        .post(`${process.env.VUE_APP_BACKEND_URL}/api/like/${this.$route.params.photoId}?userId=${userId}`)
        .then(() => {
          if (this.isLiked) {
            this.likesCount -= 1;
          } else {
            this.likesCount += 1;
          }
          this.isLiked = !this.isLiked;
        })
        .catch((error) => {
          console.error('좋아요 상태 변경에 실패했습니다.', error);
        });
    },

    // 날짜 포맷팅
    formatDate(dateString) {
      if (!dateString || dateString === 'null') {
        return 'N/A'; // null 또는 'null' 문자열일 경우 N/A로 처리
      }

      const date = new Date(dateString); // 전달된 문자열을 Date 객체로 변환

      if (isNaN(date.getTime())) {
        // 만약 잘못된 날짜 값이면 N/A 반환
        return 'N/A';
      }

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`; // 'YYYY-MM-DD' 형식으로 반환
    },
  },

  mounted() {
    const photoId = this.$route.params.photoId;
    if (photoId) {
      console.log('Mounted is triggered. Fetching comments...');
      this.fetchPhotoDetail(photoId);
    } else {
      console.error('photoId가 정의되지 않았습니다.');
    }
  },
};
</script>

<style scoped>
.main-container {
position: relative;
width: 100%;
height: auto;
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

.left-side {
margin-right: 180px;
margin-top: 30px;
width: 600px;
height: 800px;
display: flex;
justify-content: center;
align-items: center; 
}

.photo-image {
position: relative;
background-size: contain;
background-position: center;
background-repeat: no-repeat;
}

.right-side {
width: 100%;
max-width: 500px;
display: flex;
flex-direction: column;
margin: 30px;
}

.title-like-container {
display: flex;
justify-content: space-between; /* 방 이름과 좋아요 버튼을 양쪽에 배치 */
align-items: center; /* 수직 중앙 정렬 */
}

.like-section {
display: flex;
align-items: center; /* 수직 중앙 정렬 */
margin-right: 15px;
}

.middleTitle {
text-align: left;
  margin: 0;
  font-size: 25px;
}

.smallTitle {
  font-size: 20px;
  color: #db574d;
}

button {
background: none;
border: none;
cursor: pointer;
font-size: 18px;
color: #db574d;
}

button.liked {
color: red;
font-size: 24px;
}

.like-count {
font-size: 20px;
margin-left: 10px;
}

.comment-section {
margin: 10px;
}

.comment-header {
display: flex;
justify-content: space-between;
align-items: center;
margin-top: 20px;
margin-bottom: 10px;
}

.comment-list {
list-style-type: none;
padding: 0;
margin: 0;
}

.comment-item {
display: flex;
justify-content: space-between;
align-items: center;
border-bottom: 1px solid #ddd;
padding: 5px 0;
}

.nickName {
  font-size: 20px;
}

.comment-content {
flex: 1;
font-size: 16px;
}

.comment-date {
font-size: 16px;
color: #666;
}

textarea {
width: 100%;
height: 60px; /* Adjust height as needed */
margin: 10px 0;
padding: 5px;
box-sizing: border-box;
background-color: transparent;
}

.dropdown-toggle {
background-color: transparent !important; /* 배경 투명 */
border: none !important;
font-size: 14px;
cursor: pointer;
margin-left: 10px;
color: #666 !important; /* 삼각형 색상 설정 */
}

.right {
text-align: right;
}

.divider {
border: none;
border-top: 1px solid #bcbcbc; /* 선의 색상과 두께 설정 */
margin: 20px 0; /* 구분선의 위아래 간격 설정 */
}

.like-heart-image {
width: 20px; /* 하트 아이콘 크기 */
height: 18px;
cursor: pointer;
}
</style>
