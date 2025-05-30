<script setup>
</script>

<template>
  <div class="not-found-container">
    <div class="not-found-message">
      <h1>404</h1>
      <p>Oops! The page you're looking for doesn't exist.</p>
      <router-link to="/" class="back-home">Go back to Home</router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f0f0;
  font-family: Arial, sans-serif;
}

.not-found-message {
  text-align: center;
  background-color: #fff;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.not-found-message h1 {
  font-size: 100px;
  color: #ff6f61;
  margin: 0;
}

.not-found-message p {
  font-size: 18px;
  color: #333;
  margin: 20px 0;
}

.back-home {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #ff6f61;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  font-size: 16px;
  transition: background-color 0.3s ease;
}

.back-home:hover {
  background-color: #e65c50;
}
</style>
