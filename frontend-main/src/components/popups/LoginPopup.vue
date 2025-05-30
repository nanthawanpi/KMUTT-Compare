<script setup>
import { useUIStore } from '@/stores/uiStore';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthorize } from '@/stores/authorize';

const useAuthor = useAuthorize();
const { setRole } = useAuthor;
const token = ref(null);
const refreshToken = ref(null);

const FETCH_API = import.meta.env.VITE_API_ROOT;
const router = useRouter();
const username = ref('');
const password = ref('');
const statusCode = ref(0);
const errText = ref('');
const activeClass = ref(false);
const className = ref('');
const warning = ref(false);
const uiStore = useUIStore();

// State เพื่อตรวจจับว่าผู้ใช้เริ่มพิมพ์หรือยัง
const isUsernameDirty = ref(false);
const isPasswordDirty = ref(false);

// ตรวจสอบความถูกต้องของข้อมูลที่ป้อน
const usernameError = computed(() => {
  if (!isUsernameDirty.value) return ''; // ถ้ายังไม่พิมพ์ ไม่ต้องแสดง error
  if (!username.value) return 'Please enter your username.';
  return '';
});

const passwordError = computed(() => {
  if (!isPasswordDirty.value) return ''; // ถ้ายังไม่พิมพ์ ไม่ต้องแสดง error
  if (!password.value) return 'Please enter your password.';
  return '';
});

const isFormValid = computed(() => !usernameError.value && !passwordError.value);

const switchPopup = () => {
  uiStore.closeLoginPopup();
  uiStore.openRegisPopup();
};

const login = async () => {
  isUsernameDirty.value = true;
  isPasswordDirty.value = true;

  if (!isFormValid.value) {
    warning.value = true;
    return;
  }

  let user = {
    username: username.value.trim(),
    password: password.value.trim()
  };

  try {
    const res = await fetch(FETCH_API + '/auth/login', {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(user)
    });

    if (res.status === 200) {
      statusCode.value = 200;
      activeClass.value = false;
      className.value = 'alert-success';

      const tokens = await res.json();
      token.value = tokens.accessToken;
      refreshToken.value = tokens.refreshToken;
      setRole(token.value)

      localStorage.setItem("token", token.value);
      localStorage.setItem("refreshToken", refreshToken.value);

      // console.log("Token ตอน login: "+ token.value)
      // console.log("refreshToken ตอน login: "+ refreshToken.value)

      uiStore.closeLoginPopup();

    } else{
      errText.value = 'Invalid username or password';
      activeClass.value = true;
      className.value = 'alert-error';
    }

    warning.value = true;
  } catch (error) {
    // console.log('error ', error);
    router.push('/Home');
  }
};
</script>

<template>
  <div id="login-popup" tabindex="-1"
      class="bg-black/50 overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 h-full items-center justify-center flex">
      <div class="relative p-4 w-full max-w-md h-full md:h-auto">

          <div class="relative bg-white rounded-lg shadow">
              
              <button type="button"
                  @click="uiStore.closeLoginPopup();"
                  class="absolute top-3 right-2.5 text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center popup-close">
                  <svg aria-hidden="true" class="w-5 h-5" fill="#c6c7c7" viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg">
                      <path fill-rule="evenodd"
                          d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                          cliprule="evenodd"></path>
                  </svg>
                  <span class="sr-only">Close popup</span>
              </button>

              <div class="p-5">
                  <h3 class="text-2xl mb-0.5 font-medium"></h3>
                  <p class="mb-4 text-sm font-normal text-gray-800"></p>

                  <div class="text-center">
                      <p class="mb-3 text-2xl font-semibold leading-5 text-slate-900">
                          เข้าสู่ระบบ
                      </p>
                      <p class="mt-2 text-sm leading-4 text-slate-600">
                        KMUTT Compare - JIP.
                    </p>
                  </div>

                  <form class="w-full mt-7" @submit.prevent="login">
                      <label for="username" class="sr-only">Username</label>
                      <input v-model="username" name="username" type="text" autocomplete="username" 
                          @input="isUsernameDirty = true"
                          class="block w-full rounded-lg border border-gray-300 px-3 py-2 shadow-sm outline-none placeholder:text-gray-400 focus:ring-2 focus:ring-black focus:ring-offset-1"
                          placeholder="Username">

                      <label for="password" class="sr-only">Password</label>
                      <input v-model="password" name="password" type="password" autocomplete="current-password"
                          @input="isPasswordDirty = true"
                          class="mt-2 block w-full rounded-lg border border-gray-300 px-3 py-2 shadow-sm outline-none placeholder:text-gray-400 focus:ring-2 focus:ring-black focus:ring-offset-1"
                          placeholder="Password">
                      <p v-if="errText" class="text-red-500 text-sm mt-1">{{ errText }}</p>

                      <p class="mb-3 mt-2 text-sm text-gray-500">
                          <a href="/forgot-password" class="pl-1 text-blue-800 hover:text-blue-600">ลืมรหัสผ่าน</a>
                      </p>
                      <button type="submit"
                          :disabled="!isFormValid"
                          class="inline-flex w-full items-center justify-center rounded-lg bg-black p-2 py-3 text-sm font-medium text-white outline-none focus:ring-2 focus:ring-black focus:ring-offset-1 disabled:bg-gray-400">
                          เข้าสู่ระบบ
                      </button>
                  </form>

                  <div class="mt-6 text-center text-sm text-slate-600">
                    ยังไม่มีบัญชีช่ไหม?
                    <button @click="switchPopup" class="text-blue-600">
                        สมัครบัญชี
                    </button>
                </div>
              </div>
          </div>
      </div>
  </div>
</template>

