import { create } from 'zustand';

interface ModalState {
  isLeafExistModalOpen: boolean;
  isNoLeafExistModalOpen: boolean;
  isLeafDeleteModalOpen: boolean;
  setIsLeafExistModalOpen: (isLeafExistModalOpen: boolean) => void;
  setIsNoLeafExistModalOpen: (isNoLeafExistModalOpen: boolean) => void;
  setIsLeafDeleteModalOpen: (isNoLeafExistModalOpen: boolean) => void;
}

const useModalStore = create<ModalState>((set) => ({
  isLeafExistModalOpen: false,
  isNoLeafExistModalOpen: false,
  isLeafDeleteModalOpen: false,
  setIsLeafExistModalOpen: (isLeafExistModalOpen) =>
    set(() => ({ isLeafExistModalOpen })),
  setIsNoLeafExistModalOpen: (isNoLeafExistModalOpen) =>
    set(() => ({ isNoLeafExistModalOpen })),
  setIsLeafDeleteModalOpen: (isLeafDeleteModalOpen) =>
    set(() => ({ isLeafDeleteModalOpen })),
}));

export default useModalStore;
