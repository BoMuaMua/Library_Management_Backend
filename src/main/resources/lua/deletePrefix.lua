-- 获取传入的匹配前缀（通常通过 ARGV[1] 传进来，例如 "user:*"）
local keys = redis.call('keys', ARGV[1])

-- 如果找到了匹配的 key，则调用 del 命令批量删除
if #keys > 0 then
    return redis.call('del', unpack(keys))
else
    -- 如果没有找到，返回 0
    return 0
end
