var sessionsArray = {};
sessionsArray.keys = [];
const cookieNamePrivateKey = 'PRIVATE-TOKEN';
const cookieNameUserID = 'USER-ID';

exports.sessionArray = {
    array: sessionsArray,
    keys: sessionsArray.keys,
};
exports.cookie = {
    cookieNamePrivateKey: cookieNamePrivateKey,
    cookieNameUserID: cookieNameUserID
};
