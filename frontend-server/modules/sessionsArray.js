var sessionsArray = {};
sessionsArray.keys = [];
const cookieNamePrivateKey = 'PRIVATE-TOKEN';

exports.sessionArray = {
    array: sessionsArray,
    keys: sessionsArray.keys,
};
exports.cookie = {
    cookieNamePrivateKey: cookieNamePrivateKey
};
