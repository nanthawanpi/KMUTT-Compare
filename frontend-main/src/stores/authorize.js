import { defineStore, acceptHMRUpdate } from 'pinia';
import { ref, watch } from 'vue';
import { jwtDecode } from 'jwt-decode'; // ✅ Named import for v4.0.0

export const useAuthorize = defineStore('authorize', () => {
  const userRole = ref(localStorage.getItem("userRole") ||'guest');
  const username = ref(localStorage.getItem("username"));
  const userId = ref(localStorage.getItem("userId"));


  const setRole = (token) => {
    if (!token) {  // ตรวจสอบว่า token เป็น null, undefined, หรือ empty string
      userRole.value = 'guest';
      localStorage.setItem("userRole", 'guest');
      // console.log("No token provided, setting userRole to guest");
      return;
    }

    // ถ้ามี token ให้ decode token เพื่อดึง role และ username
    const decoded = jwtDecode(token);
    // console.log(decoded)

    userRole.value = decoded.role
    username.value = decoded.sub;
    userId.value = decoded.userId

    localStorage.setItem("userRole", userRole.value);  // เก็บ role ที่ถูกต้องใน localStorage
    localStorage.setItem("username", username.value);
    localStorage.setItem("userId", userId.value);
    // console.log(userId.value)
  };

  // ตรวจจับการเปลี่ยนแปลงใน localStorage และอัปเดตค่า userRole และ username
  watch(() => localStorage.getItem("userRole"), (newValue) => {
    if (newValue !== userRole.value) {
      userRole.value = newValue;
    }
  });

  return { userRole, setRole, username, userId };
});

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthorize, import.meta.hot));
}
