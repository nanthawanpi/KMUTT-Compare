import { useAuthorize } from '@/stores/authorize';
import { createRouter, createWebHistory } from 'vue-router';
import { storeToRefs } from 'pinia';
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/UserView/HomeView.vue')
    },
    {
      path: '/compare',
      name: 'compare',
      component: () => import('../views/UserView/ComparePage.vue')
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('../views/UserView/FavoritesView.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/faq',
      name: 'faq',
      component: () => import('../views/UserView/FAQ.vue')
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('../views/UserView/Contact.vue')
    },
    {
      path: '/dormitory/:id',
      name: 'dormitoryDetail',
      component: () => import('../views/UserView/DormitoryDetail.vue')
    },
    {
      path: '/addEditDormitory/:id?',
      name: 'addEditDormitory',
      component: () => import('../views/ManageDorrm/AddEditDormitory.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/UserView/Setting/Profile.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/dormlists',
      name: 'dormlists',
      component: () => import('../views/UserView/Setting/DormLists.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/reservation/:id?/:action?',
      name: 'reservation',
      component: () => import('../views/ManageDorrm/AddEditReservation.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/reservedForms',
      name: 'reservedForms',
      component: () => import('../views/UserView/Setting/ReservedForms.vue'),
      props: true, // เปิดให้รับค่า params เป็น props
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/receivedForms',
      name: 'receivedForms',
      component: () => import('../views/UserView/Setting/ReceivedForm.vue'),
      props: true, // เปิดให้รับค่า params เป็น props
      meta: { requiresUserOrAdmin: true }
    },
    {
      path: '/support',
      name: 'support',
      component: () => import('../views/UserView/Setting/Support.vue')
    },
    {
      path: '/admin/dashboard',
      name: 'dashboard',
      component: () => import('../views/AdminView/Dashboard.vue'),
      meta: { requiresAdmin: true } // เพิ่ม meta เพื่อกำหนดว่าเป็น route ที่ต้องการสิทธิ์
    },
    {
      path: '/admin/user-management',
      name: 'user-management',
      component: () => import('../views/AdminView/UsersManagement/ListAllUsers.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/add-edit-user/:id?',
      name: 'AdminEditUser',
      component: () => import('../views/AdminView/UsersManagement/AdminAddEditUser.vue'),
      meta: { requiresAdmin: true }
    },
    {
      path: '/admin/dormitory-management',
      name: 'dormitory-management',
      component: () => import('../views/AdminView/DormitoryManagement.vue'),
      meta: { requiresUserOrAdmin: true }
    },
    // 404 route - ใช้ pathMatch(.*)* สำหรับจับทุก URL
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: () => import('../views/Notfound.vue')
    }
  ]
})

// การใช้งาน beforeEach guard เพื่อตรวจสอบการเข้าถึง
router.beforeEach((to, from, next) => {
  // ถ้าหน้า route ที่ต้องการ user หรือ admin เข้าถึง
  const myRole = useAuthorize()
  const {userRole} = storeToRefs(myRole)

  if (to.meta.requiresUserOrAdmin) {
    if (userRole.value === 'user' || userRole.value === 'admin') {
      // console.log(userRole.value)
      next(); 
    } else {
      // console.log(userRole.value)
      alert('คุณไม่มีสิทธิ์เข้าถึงหน้านี้');
      next({ name: '404' }); // ไปหน้า 404
    }
  }else if(to.meta.requiresAdmin){
    if (userRole.value === 'admin') { 
      next();
    } else {
      alert('คุณไม่มีสิทธิ์เข้าถึงหน้านี้');
      next({ name: '404' }); // ไปหน้า 404
    }
  }else {
      next();
    }
});

export default router;
