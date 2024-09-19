<template>
  <div>
    <p class="middleTitle">인기 사진</p>
    <div class="carousel-container">
      <div class="carousel">
        <div
          class="slides"
          :style="{ transform: `translateX(-${currentIndex * (100 / itemsPerSlide)}%)` }"
        >
          <img
            v-for="(photo, index) in photos"
            :key="index"
            :src="photo"
            :alt="'Photo ' + index"
            class="photo"
          />
        </div>
      </div>
      <button @click="prevSlide" class="nav-btn prev-btn">‹</button>
      <button @click="nextSlide" class="nav-btn next-btn">›</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      photos: [],
      currentIndex: 0,
      itemsPerSlide: 5,
    };
  },
  mounted() {
    axios
      .get(`${process.env.VUE_APP_BACKEND_URL}/photo/hot`)
      .then((response) => {
        this.photos = response.data
          .map((photo) => photo.completePhoto)
          .filter((photo) => photo !== null);
          console.log(response.data);
      })
      .catch((error) => {
        console.error('Error fetching photos:', error);
      });
      
  },
  computed: {
    totalSlides() {
      return Math.ceil(this.photos.length / this.itemsPerSlide);
    },
  },
  methods: {
    nextSlide() {
      if (this.currentIndex < this.totalSlides - 1) {
        this.currentIndex++;
      }
    },
    prevSlide() {
      if (this.currentIndex > 0) {
        this.currentIndex--;
      }
    },
  },
};
</script>

<style>
.carousel-container {
  position: relative;
  width: 100%;
  height: 400px;
  margin: auto;
  overflow: hidden;
}

.carousel {
  display: flex;
  width: 100%;
  overflow: hidden;
}

.slides {
  display: flex;
  transition: transform 0.5s ease-in-out;
  gap: 30px;
}

.photo {
  width: auto;
  height: 400px;
  flex: 0 0 auto;
  object-fit: cover;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  padding: 0 10px;
  z-index: 10;
}

.prev-btn {
  left: 10px;
}

.next-btn {
  right: 10px;
}

</style>
