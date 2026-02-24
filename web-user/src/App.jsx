import { Navigate, Route, Routes } from "react-router-dom";
import NavBar from "./components/NavBar.jsx";
import HomePage from "./pages/HomePage.jsx";
import AdoptionPage from "./pages/AdoptionPage.jsx";
import ServicesPage from "./pages/ServicesPage.jsx";
import AdoptionDetailPage from "./pages/AdoptionDetailPage.jsx";
import RehomePostCreatePage from "./pages/RehomePostCreatePage.jsx";
import MyRehomePostsPage from "./pages/MyRehomePostsPage.jsx";
import MyRehomePostApplicationsPage from "./pages/MyRehomePostApplicationsPage.jsx";
import MyAdoptionApplicationsPage from "./pages/MyAdoptionApplicationsPage.jsx";
import AdminAdoptionPostReviewListPage from "./pages/AdminAdoptionPostReviewListPage.jsx";
import AdminAdoptionPostReviewDetailPage from "./pages/AdminAdoptionPostReviewDetailPage.jsx";

function App() {
  return (
    <div className="app-shell">
      <div className="bg-orb bg-orb-left" />
      <div className="bg-orb bg-orb-right" />
      <NavBar />
      <main className="page-main container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/adoption" element={<AdoptionPage />} />
          <Route path="/adoption/:postId" element={<AdoptionDetailPage />} />
          <Route path="/adoption/rehome/new" element={<RehomePostCreatePage />} />
          <Route path="/me/adoption/posts" element={<MyRehomePostsPage />} />
          <Route
            path="/me/adoption/posts/:postId/applications"
            element={<MyRehomePostApplicationsPage />}
          />
          <Route path="/me/adoption/applications" element={<MyAdoptionApplicationsPage />} />
          <Route path="/admin/adoptions/posts" element={<AdminAdoptionPostReviewListPage />} />
          <Route
            path="/admin/adoptions/posts/:postId"
            element={<AdminAdoptionPostReviewDetailPage />}
          />
          <Route path="/services" element={<ServicesPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="site-footer container">
        <p>PetLove Web MVP • 温暖治愈 + 专业可信</p>
      </footer>
    </div>
  );
}

export default App;
