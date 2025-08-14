import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta:{
        showLayout: true
      }
    },
    {
      path: '/problems',
      name: 'problems',
      component: () => import('../views/ProblemsView.vue'),
      meta:{
        showLayout: true
      }
    },
    {
      path: '/contests',
      name: 'contests',
      component: () => import('../views/ContestsView.vue'),
      meta:{
        showLayout: true
      }
    },
    {
      path: '/submissions',
      name: 'submissions',
      component: () => import('../views/SubmissionsView.vue'),
      meta:{
        showLayout: true
      }
    },
    // {
    //   path: '/ranking',
    //   name: 'ranking',
    //   component: () => import('../views/RankingView.vue'),
    //   meta:{
    //     showLayout: true
    //   }
    // },
    {
      path: '/discussion',
      name: 'discussion',
      component: () => import('../views/DiscussionView.vue'),
      meta:{
        showLayout: true
      }
    },
    {
      path: '/feedback',
      name: 'feedback',
      component: () => import('../views/FeedbackView.vue'),
      meta:{
        showLayout: true
      }
    },
    {
      path: '/loginORegister',
      name: 'loginORegister',
      component: () => import('../views/LoginOrRegisterView.vue'),
      meta:{
        showLayout: true
      }
    },
    {
      path: '/problemPage/:id',
      name: 'problemPage',
      component: () => import('../views/ProblemPageView.vue'),
      meta:{
        showLayout: false
      }
    },
    {
      path: '/submission-result',
      name: 'submissionResult',
      component: () => import('../components/Submission/SubmissionResultView.vue'),
      meta: {
        showLayout: false
      }
    },
    {
      path: '/submission/:id',
      name: 'submissionDetail',
      component: () => import('../components/Submission/SubmissionDetailView.vue'),
      meta: {
        showLayout: true
      }
    },
  ]
})

export default router
