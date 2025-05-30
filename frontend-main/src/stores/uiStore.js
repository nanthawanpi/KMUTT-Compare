// stores/uiStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUIStore = defineStore('ui', () => {
  // State
  const isLoginPopupOpen = ref(false);
  const isRegisPopupOpen = ref(false)

  // Actions
  const openLoginPopup = () => {
    isLoginPopupOpen.value = true;
  };

  const closeLoginPopup = () => {
    isLoginPopupOpen.value = false;
  };

  const openRegisPopup = () => {
    isRegisPopupOpen.value = true;
  };

  const closeRegisPopup = () => {
    isRegisPopupOpen.value = false;
  };
  



  return {
    isLoginPopupOpen,
    openLoginPopup,
    closeLoginPopup,

    isRegisPopupOpen,
    openRegisPopup,
    closeRegisPopup
  };
});
