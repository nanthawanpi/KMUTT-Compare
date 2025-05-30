<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  modelValue: Boolean,
  minPrice: Number,
  maxPrice: Number,
  selectTypes: String,
  selectedDistance: Number,
});

const emit = defineEmits(['update:minPrice', 'update:maxPrice', 'update:selectTypes', 'update:selectedDistance', 'close']);
</script>

<template>
    <div class="popup-overlay">
      <div class="filter">
        <!-- ราคา -->
        <div>
          <label>ราคาเริ่มต้น: {{ minPrice }} ฿</label>
          <input
            type="range"
            :value="minPrice"
            @input="$emit('update:minPrice', +$event.target.value)"
            min="0"
            max="30000"
            step="100"
          />
  
          <label>ราคาสูงสุด: {{ maxPrice }} ฿</label>
          <input
            type="range"
            :value="maxPrice"
            @input="$emit('update:maxPrice', +$event.target.value)"
            min="0"
            max="30000"
            step="100"
          />
        </div>
  
        <!-- ประเภทหอพัก -->
        <div>
          <h3>ประเภทหอพัก</h3>
          <label>
            <input type="radio" value="" :checked="selectTypes === ''" @change="$emit('update:selectTypes', '')" />
            ทั้งหมด
          </label>
          <label>
            <input type="radio" value="m" :checked="selectTypes === 'm'" @change="$emit('update:selectTypes', 'm')" />
            ชาย
          </label>
          <label>
            <input type="radio" value="f" :checked="selectTypes === 'f'" @change="$emit('update:selectTypes', 'f')" />
            หญิง
          </label>
        </div>
  
        <!-- ระยะทาง -->
        <div>
          <h3>ระยะทาง</h3>
          <select :value="selectedDistance" @change="$emit('update:selectedDistance', +$event.target.value)">
            <option value="0">ไม่จำกัด</option>
            <option value="1">น้อยกว่า 1 กม.</option>
            <option value="2">2 -> 3 กม.</option>
            <option value="3">3 -> 4 กม.</option>
            <option value="4">4 -> 5 กม.</option>
            <option value="5">5 กม. ขึ้นไป</option>
          </select>
        </div>
  
        <!-- ปุ่มปิด -->
        <div>
          <button @click="$emit('close')">ปิด</button>
        </div>
      </div>
    </div>
  </template>
  
