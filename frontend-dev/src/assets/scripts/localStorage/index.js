/**
 * get the roomId from localStorage
 * @return {number} roomId
 */
function getRoomId() {
    return Number.parseInt(sessionStorage.getItem('roomId'));
}

/**
 * set the roomId to localStorage
 * @param {number} roomId
 */
function setRoomId(roomId) {
    sessionStorage.setItem('roomId', roomId.toString());
}

export {
    getRoomId,
    setRoomId
}