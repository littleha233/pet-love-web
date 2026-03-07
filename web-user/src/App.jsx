import { Navigate, Route, Routes } from "react-router-dom";
import NavBar from "./components/NavBar.jsx";
import HomePage from "./pages/HomePage.jsx";
import AdoptionModulePage from "./pages/AdoptionModulePage.jsx";
import AdoptionPage from "./pages/AdoptionPage.jsx";
import AdoptionDetailPage from "./pages/AdoptionDetailPage.jsx";
import RehomePostCreatePage from "./pages/RehomePostCreatePage.jsx";
import MyRehomePostsPage from "./pages/MyRehomePostsPage.jsx";
import MyRehomePostApplicationsPage from "./pages/MyRehomePostApplicationsPage.jsx";
import MyAdoptionApplicationsPage from "./pages/MyAdoptionApplicationsPage.jsx";
import AdminAdoptionPostReviewListPage from "./pages/AdminAdoptionPostReviewListPage.jsx";
import AdminAdoptionPostReviewDetailPage from "./pages/AdminAdoptionPostReviewDetailPage.jsx";
import RescueHomePage from "./pages/RescueHomePage.jsx";
import RescueGuideListPage from "./pages/RescueGuideListPage.jsx";
import RescueGuideDetailPage from "./pages/RescueGuideDetailPage.jsx";
import RescueResourceListPage from "./pages/RescueResourceListPage.jsx";
import RescueResourceDetailPage from "./pages/RescueResourceDetailPage.jsx";
import RescueClueSubmitPage from "./pages/RescueClueSubmitPage.jsx";
import MyRescueCluesPage from "./pages/MyRescueCluesPage.jsx";
import MyRescueClueDetailPage from "./pages/MyRescueClueDetailPage.jsx";
import AdminRescueGuideListPage from "./pages/AdminRescueGuideListPage.jsx";
import AdminRescueGuideEditPage from "./pages/AdminRescueGuideEditPage.jsx";
import AdminRescueResourceListPage from "./pages/AdminRescueResourceListPage.jsx";
import AdminRescueResourceEditPage from "./pages/AdminRescueResourceEditPage.jsx";
import AdminRescueClueListPage from "./pages/AdminRescueClueListPage.jsx";
import AdminRescueClueDetailPage from "./pages/AdminRescueClueDetailPage.jsx";
import ComplaintSubmitPage from "./pages/ComplaintSubmitPage.jsx";
import MyComplaintTicketsPage from "./pages/MyComplaintTicketsPage.jsx";
import MyComplaintTicketDetailPage from "./pages/MyComplaintTicketDetailPage.jsx";
import AdminOpsDashboardPage from "./pages/AdminOpsDashboardPage.jsx";
import AdminComplaintTicketListPage from "./pages/AdminComplaintTicketListPage.jsx";
import AdminComplaintTicketDetailPage from "./pages/AdminComplaintTicketDetailPage.jsx";
import AdminBlacklistPage from "./pages/AdminBlacklistPage.jsx";
import AdminCityFeaturePage from "./pages/AdminCityFeaturePage.jsx";
import AdminAuditLogSearchPage from "./pages/AdminAuditLogSearchPage.jsx";
import MobileLoginPage from "./pages/MobileLoginPage.jsx";
import CommunityPage from "./pages/CommunityPage.jsx";
import MyCenterPage from "./pages/MyCenterPage.jsx";

function App() {
  return (
    <div className="app-shell">
      <div className="bg-orb bg-orb-left" />
      <div className="bg-orb bg-orb-right" />
      <div className="bg-orb bg-orb-center" />
      <NavBar />
      <main className="page-main container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/me" element={<MyCenterPage />} />
          <Route path="/auth/mobile-login" element={<MobileLoginPage />} />

          <Route path="/adoption" element={<AdoptionModulePage />} />
          <Route path="/adoption/list" element={<AdoptionPage />} />
          <Route path="/adoption/post/new" element={<RehomePostCreatePage />} />
          <Route path="/adoption/applications" element={<MyAdoptionApplicationsPage />} />
          <Route path="/adoption/my-posts" element={<MyRehomePostsPage />} />
          <Route
            path="/adoption/my-posts/:postId/applications"
            element={<MyRehomePostApplicationsPage />}
          />
          <Route path="/adoption/:postId" element={<AdoptionDetailPage />} />

          <Route path="/feeding/*" element={<Navigate to="/adoption" replace />} />

          <Route path="/adoption/rehome/new" element={<Navigate to="/adoption/post/new" replace />} />
          <Route path="/me/adoption/posts" element={<MyRehomePostsPage />} />
          <Route
            path="/me/adoption/posts/:postId/applications"
            element={<MyRehomePostApplicationsPage />}
          />
          <Route path="/me/adoption/applications" element={<MyAdoptionApplicationsPage />} />
          <Route path="/me/feeding/*" element={<Navigate to="/adoption" replace />} />
          <Route path="/provider/feeding/*" element={<Navigate to="/adoption" replace />} />

          <Route path="/rescue" element={<RescueHomePage />} />
          <Route path="/rescue/guides" element={<RescueGuideListPage />} />
          <Route path="/rescue/guides/:guideId" element={<RescueGuideDetailPage />} />
          <Route path="/rescue/resources" element={<RescueResourceListPage />} />
          <Route path="/rescue/resources/:resourceId" element={<RescueResourceDetailPage />} />
          <Route path="/rescue/clues/new" element={<RescueClueSubmitPage />} />
          <Route path="/me/rescue/clues" element={<MyRescueCluesPage />} />
          <Route path="/me/rescue/clues/:clueId" element={<MyRescueClueDetailPage />} />
          <Route path="/support/complaints/new" element={<ComplaintSubmitPage />} />
          <Route path="/me/support/complaints" element={<MyComplaintTicketsPage />} />
          <Route path="/me/support/complaints/:ticketId" element={<MyComplaintTicketDetailPage />} />

          <Route path="/admin/adoptions/posts" element={<AdminAdoptionPostReviewListPage />} />
          <Route
            path="/admin/adoptions/posts/:postId"
            element={<AdminAdoptionPostReviewDetailPage />}
          />
          <Route path="/admin/rescue/guides" element={<AdminRescueGuideListPage />} />
          <Route path="/admin/rescue/guides/:guideId" element={<AdminRescueGuideEditPage />} />
          <Route path="/admin/rescue/resources" element={<AdminRescueResourceListPage />} />
          <Route path="/admin/rescue/resources/:resourceId" element={<AdminRescueResourceEditPage />} />
          <Route path="/admin/rescue/clues" element={<AdminRescueClueListPage />} />
          <Route path="/admin/rescue/clues/:clueId" element={<AdminRescueClueDetailPage />} />
          <Route path="/admin/ops/dashboard" element={<AdminOpsDashboardPage />} />
          <Route path="/admin/ops/complaints" element={<AdminComplaintTicketListPage />} />
          <Route path="/admin/ops/complaints/:ticketId" element={<AdminComplaintTicketDetailPage />} />
          <Route path="/admin/ops/risk/blacklists" element={<AdminBlacklistPage />} />
          <Route path="/admin/ops/city-features" element={<AdminCityFeaturePage />} />
          <Route path="/admin/ops/audit-logs" element={<AdminAuditLogSearchPage />} />

          <Route path="/services" element={<Navigate to="/rescue" replace />} />
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
