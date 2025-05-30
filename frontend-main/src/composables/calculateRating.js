import { ref } from 'vue';

export function calculateRating() {
  const isModalOpen = ref(false);  // ควบคุมการแสดงผล Modal
  const selectedScore = ref(0);    // เก็บคะแนนที่เลือก

  const openModal = () => {
    isModalOpen.value = true;
  };

  const closeModal = () => {
    isModalOpen.value = false;
    selectedScore.value = 0;  // รีเซ็ตคะแนน
  };

  const submitRating = async (formId) => {
    if (!selectedScore.value) return; // ป้องกันการส่งคะแนน 0

    try {
      const response = await fetch('/api/user/forms/vote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: formId, score: selectedScore.value })
      });

      if (!response.ok) {
        throw new Error('Failed to submit rating');
      }

      closeModal();  // ปิด Modal เมื่อส่งเสร็จ
    } catch (error) {
      // console.error('Error submitting rating:', error);
    }
  };

  return { isModalOpen, selectedScore, openModal, closeModal, submitRating };
}
