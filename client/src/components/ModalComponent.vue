<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal">
      <!-- 동적으로 전달된 메시지를 표시 -->
      <p v-if="messageType === 'confirm'" class="confirm-text">{{ message }}</p>
      <p v-if="messageType === 'completion'" class="completion-text">{{ message }}</p>
      <div v-if="messageType === 'confirm'" class="modal-actions">
        <button @click="onConfirm" class="confirm-button">네</button>
        <button @click="closeModal" class="cancel-button">아니오</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    messageType: {
      type: String,
      required: true, // "confirm" 또는 "completion"
    },
    message: {
      type: String,
      required: true, // 동적으로 전달될 메시지
    },
    onConfirm: {
      type: Function,
      required: false,
    },
  },
  methods: {
    closeModal() {
      this.$emit('close');
    },
  },
};
</script>

<style scoped>
/* 스타일은 기존 그대로 유지 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background-color: #EEEDEE;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  width: 300px;
  height: 200px;
}

.modal p {
  font-size: 22px;
  margin-top: 40px;
}

.modal-actions {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.confirm-text {
  margin-bottom: 50px;
}

.confirm-button,
.cancel-button {
  border: none;
  padding: 10px 20px;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
}

.confirm-button {
  background-color: transparent;
  color: #DB574D;
  font-size: 18px;
}

.cancel-button {
  background-color: transparent;
  color: #DB574D;
  font-size: 18px;
}

.confirm-button:hover {
  background-color: #DB574D;
  color: white;
}

.cancel-button:hover {
  background-color: #DB574D;
  color: white;
}
</style>
