<script setup>
import Navbar from './components/Navbar.vue';
import FooterBar from './components/FooterBar.vue';
import { RouterView } from 'vue-router';

</script>
 
<template>
<div class="font-itim-regular">
    <Navbar/>
    <router-view></router-view>
    <FooterBar/>
</div>
</template>
 
<style scoped>

</style>