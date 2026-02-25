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
import FeedingProviderListPage from "./pages/FeedingProviderListPage.jsx";
import FeedingProviderDetailPage from "./pages/FeedingProviderDetailPage.jsx";
import FeedingOrderCreatePage from "./pages/FeedingOrderCreatePage.jsx";
import MyFeedingOrdersPage from "./pages/MyFeedingOrdersPage.jsx";
import MyFeedingOrderDetailPage from "./pages/MyFeedingOrderDetailPage.jsx";
import FeedingProviderCenterPage from "./pages/FeedingProviderCenterPage.jsx";
import FeedingProviderOrdersPage from "./pages/FeedingProviderOrdersPage.jsx";
import FeedingProviderOrderDetailPage from "./pages/FeedingProviderOrderDetailPage.jsx";
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

          <Route path="/feeding/providers" element={<FeedingProviderListPage />} />
          <Route path="/feeding/providers/:providerUserId" element={<FeedingProviderDetailPage />} />
          <Route path="/feeding/orders/new" element={<FeedingOrderCreatePage />} />
          <Route path="/me/feeding/orders" element={<MyFeedingOrdersPage />} />
          <Route path="/me/feeding/orders/:orderId" element={<MyFeedingOrderDetailPage />} />
          <Route path="/provider/feeding/profile" element={<FeedingProviderCenterPage />} />
          <Route path="/provider/feeding/orders" element={<FeedingProviderOrdersPage />} />
          <Route path="/provider/feeding/orders/:orderId" element={<FeedingProviderOrderDetailPage />} />

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
