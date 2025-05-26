import React from 'react';
import { Layout, Menu } from 'antd';
import { UserOutlined, CarOutlined, WalletOutlined } from '@ant-design/icons';
import UserManagement from './UserManagement';
import CarManagement from './CarManagement';
import AccountManagement from './AccountManagement';

const { Content, Sider } = Layout;

const AdminPanel = () => {
    const [selectedKey, setSelectedKey] = React.useState('users');

    const renderContent = () => {
        switch (selectedKey) {
            case 'users':
                return <UserManagement />;
            case 'cars':
                return <CarManagement />;
            case 'accounts':
                return <AccountManagement />;
            default:
                return <UserManagement />;
        }
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider width={200} theme="light">
                <Menu
                    mode="inline"
                    selectedKeys={[selectedKey]}
                    style={{ height: '100%', borderRight: 0 }}
                    onSelect={({ key }) => setSelectedKey(key)}
                >
                    <Menu.Item key="users" icon={<UserOutlined />}>
                        Пользователи
                    </Menu.Item>
                    <Menu.Item key="cars" icon={<CarOutlined />}>
                        Автомобили
                    </Menu.Item>
                    <Menu.Item key="accounts" icon={<WalletOutlined />}>
                        Счета
                    </Menu.Item>
                </Menu>
            </Sider>
            <Layout style={{ padding: '24px' }}>
                <Content style={{ background: '#fff', padding: 24, margin: 0, minHeight: 280 }}>
                    {renderContent()}
                </Content>
            </Layout>
        </Layout>
    );
};

export default AdminPanel; 