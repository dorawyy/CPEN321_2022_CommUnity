const Router = require('express-promise-router');

const { getOffer, searchOffers, searchOffersWithTags, createOffer, updateOffer, deleteOffer} = require('./controllers/offerPostController');
const { getRequest, searchRequests, searchRequestsWithTags, createRequest, updateRequest, deleteRequest} = require('./controllers/requestPostController');
const router = Router();

// Requests
router.get('/communitypost/requests/:postId', getRequest);
router.get('/communitypost/requests/:title', searchRequests);
router.get('/communitypost/requests/:tagsList', searchRequestsWithTags);
router.post('/communitypost/requests', createRequest);
router.put('/communitypost/requests', updateRequest);
router.delete('/communitypost/requests/:postId', deleteRequest);

// Offers
router.get('/communitypost/offers/:postId', getOffer);
router.get('/communitypost/offers/:title', searchOffers);
router.get('/communitypost/offers/:tagsList', searchOffersWithTags);
router.post('/communitypost/offers', createOffer);
router.put('/communitypost/offers', updateOffer);
router.delete('/communitypost/offers/:postId', deleteOffer);

module.exports = router;
