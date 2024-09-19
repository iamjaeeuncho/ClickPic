import Vue from 'vue';
import Router from 'vue-router';

import Home from '@/views/HomePage.vue'; 
import Select from '@/views/SelectPage.vue'; 
import Make from '@/views/MakePage.vue';
import Enter from '@/views/EnterPage.vue';
import Room from '@/views/RoomPage.vue';
import Edit from '@/views/EditPage.vue'; 
import Save from '@/views/SavePage.vue';

import LoginCompo from '@/views/LoginCompo.vue'; 
import UserProfile from '@/views/UserProfile.vue'; 
import MyPageCompo from '@/views/MyPageCompo.vue';

import CommunityCompo from '@/views/CommunityCompo.vue';
import PictureDetail from '@/views/PictureDetail.vue'; 

import RegisterCompo from '@/views/RegisterCompo.vue'; 
import GeneralLoginCompo from '../views/GeneralLoginCompo.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/select',
      name: 'Select',
      component: Select
    },
    {
      path: '/make',
      name: 'Make',
      component: Make
    },
    {
      path: '/enter',
      name: 'Enter',
      component: Enter
    },
    {
      path: '/enter/:roomSession',
      name: 'Room',
      component: Room,
      props: true,
    },
    {
      path: '/edit/:roomSession',
      name: 'Edit',
      component: Edit,
      props: true,
    },
    {
      path: '/save/:roomSession',
      name: 'Save',
      component: Save,
      props: true,
    },
    {
      path: '/login',  
      name: 'Login',
      component: LoginCompo
    },
    {
      path: '/profile',  
      name: 'UserProfile',
      component: UserProfile
    },
    {
      path: '/mypage', 
      name: 'MyPage',
      component: MyPageCompo
    },
    {
      path: '/community',
      name: 'Community',
      component: CommunityCompo
    },
    
    {
      path: '/photo/:photoId',
      name: 'PictureDetail',
      component: PictureDetail,
      props: true,
    },
    { path: '/register', 
      component: RegisterCompo
    },

    { path: '/generallogin', 
      component: GeneralLoginCompo
    },
  ]
});
