Client cần phân biệt dòng nào là Response và dòng nào là ServerEvent. Quy ước: dòng có trường requestId là Response, dòng có eventType là event.
# Giao thức Client–Server
- Kết nối TCP socket, mỗi message là MỘT dòng JSON kết thúc bằng '\n', UTF-8.
- Client gửi Request:  {"requestId":"...","type":"PLACE_BID","token":"...","payload":{...}}
- Server trả Response: {"requestId":"...","success":true,"payload":{...}}
  {"requestId":"...","success":false,"error":"INVALID_BID","message":"..."}
- Server đẩy Event:    {"eventType":"BID_PLACED","payload":{...}}   (không có requestId)
- Tiền: string ("1234.50"). Thời gian: ISO-8601 UTC ("2026-01-01T20:00:00Z").
- Giờ do SERVER quyết định, client không gửi thời gian đặt giá.

| Lệnh | Vai trò | Payload | Payload trả về |
|------|---------|---------|----------------|
| LOGIN | - | LoginRequest | LoginResponse |
| PLACE_BID | BIDDER | PlaceBidRequest | BidDto |
| SUBSCRIBE | mọi người | SubscribeRequest | AuctionDto (+ sau đó nhận event) |
...(điền đủ cho mọi MessageType)