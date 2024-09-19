import Vue from 'vue';
import Vuex from 'vuex';
import axios from 'axios';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    accessToken: localStorage.getItem('accessToken') || null,
    userId: localStorage.getItem('userId') || null,  // userId를 state로 관리
    userInfo: {},
    likedPhotos: {},  // 좋아요 상태 저장
    comments: [],  // 댓글 상태 저장
  },
  mutations: {
    setAccessToken(state, token) {
      state.accessToken = token;
      localStorage.setItem('accessToken', token);
    },
    setUserId(state, userId) {  // userId를 저장하는 mutation 추가
      state.userId = userId;
      localStorage.setItem('userId', userId);
    },
    setUserInfo(state, userInfo) {
      state.userInfo = userInfo;
      const userId = userInfo.id || userInfo.userid || null;
      const nickname = userInfo.nickname || userInfo.properties?.nickname || null;
      if (userId) {
        this.commit('setUserId', userId);  // userId 설정
      }
      if (nickname) {
        localStorage.setItem('nickname', nickname);
      }
    },
    logout(state) {
      state.accessToken = null;
      state.userId = null;  // 로그아웃 시 userId도 초기화
      state.userInfo = {};
      state.likedPhotos = {}; // 로그아웃 시 좋아요 상태 초기화
      localStorage.removeItem('accessToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('nickname');
    },
    TOGGLE_LIKE(state, { photoId, isLiked }) {
      Vue.set(state.likedPhotos, photoId, isLiked);
    },
    SET_LIKED_PHOTOS(state, likedPhotos) {
      state.likedPhotos = likedPhotos;
    },
    setComments(state, comments) {
      console.log("Storing comments in state:", comments);  // 이 로그로 데이터 확인
      state.comments = comments;
    },
    addComment(state, comment) {
      state.comments.push(comment);
    },
  },
  actions: {
    fetchUserInfo({ commit, state }) {
      if (!state.accessToken) {
        return;
      }
      axios.get(`${process.env.VUE_APP_BACKEND_URL}/api/userinfo`, {
        headers: {
          Authorization: `Bearer ${state.accessToken}`
        }
      })
      .then(response => {
        commit('setUserInfo', response.data);
      })
      .catch(error => {
        console.error('사용자 정보 불러오기 실패:', error);
      });
    },
    fetchLikedPhotos({ commit }) {
      const userId = localStorage.getItem('userId');
      if (!userId) {
        return;
      }
      axios.get(`${process.env.VUE_APP_BACKEND_URL}/api/like/user/${userId}/liked-photos`)
        .then(response => {
          const likedPhotos = response.data.reduce((acc, photoId) => {
            acc[photoId] = true;
            return acc;
          }, {});
          commit('SET_LIKED_PHOTOS', likedPhotos);
        })
        .catch(error => {
          console.error('좋아요 상태 불러오기 실패:', error);
        });
    },
    toggleLike({ commit, state }, photoId) {
      const userId = localStorage.getItem('userId');
      const isLiked = !state.likedPhotos[photoId]; // 좋아요 상태 반전

      if (!userId) {
        alert('로그인이 필요합니다.');
        return;
      }

      // 서버에 좋아요/취소 요청
      axios.post(`${process.env.VUE_APP_BACKEND_URL}/api/like/${photoId}?userId=${userId}`)
        .then(() => {
          // 서버 요청 성공 후 Vuex 상태 업데이트
          commit('TOGGLE_LIKE', { photoId, isLiked });
        })
        .catch(error => {
          console.error('좋아요 상태 변경 실패:', error);
        });
    },
    fetchComments({ commit }, photoId) {
      axios.get(`/api/comments/${photoId}`)
        .then(response => {
          console.log("Fetched comments data(store):", response.data);  // 이 부분에서 전체 데이터 확인
          commit('setComments', response.data);  // 뮤테이션 호출
        })
        .catch(error => {
          console.error('댓글을 불러오는 중 오류가 발생했습니다.', error);
        });
    },
    
    addComment({ commit }, comment) {
      axios.post('/api/comments', comment)
        .then(() => {
          commit('addComment', comment);
        })
        .catch(error => {
          console.error('댓글을 추가하는 중 오류가 발생했습니다.', error);
        });
    }
  },
  getters: {
    isLiked: (state) => (photoId) => {
      return !!state.likedPhotos[photoId]; // 좋아요 여부
    },
    comments: (state) => state.comments, // 댓글 리스트 반환
  },
});
