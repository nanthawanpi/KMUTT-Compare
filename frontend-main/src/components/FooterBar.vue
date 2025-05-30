<script setup>

</script>
 
<template>
    <div class="footer">
        <div class="text-footer space-y-2">
            <h3>About Us</h3>
            <p >We help you find the best dormitory options near KMUTT with ease and convenience.</p>
        </div>
        <div class="text-footer space-y-2">
            <h3>Support</h3>
            <a href="/faq" class="text-white hover:underline">FAQ</a>
            <a href="/support" class="text-white hover:underline">GET IN TOUCH</a>
        </div>
        <div class="text-footer space-y-2">
            <h3>Contact Us</h3>
            <a href="mailto:jipjip.kmutt.compare@gmail.com" class="text-white hover:underline">
                jipjip.kmutt.compare@gmail.com
                </a>
            <div class="flex flex-row space-x-2">          
                
            </div>  
        </div>
    </div>
</template>

<style scoped>
.footer{
    margin-top: 100px;
    display: grid;
    grid-template-columns: repeat(3,1fr);
    grid-template-rows: auto;
    column-gap: 30px;
    row-gap: 20px;
    justify-items: center;
    height: 260px;
    width: 100%;
    color: white;
    background-color: black;
}

h3{
    font-size: 25px;
    padding-bottom: 10px;
}

.text-footer{
    padding-top: 50px;
}

</style>